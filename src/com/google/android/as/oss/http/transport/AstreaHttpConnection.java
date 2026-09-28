/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.as.oss.http.transport;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.apps.miphone.pcs.grpc.GrpcStatusProto;
import com.google.android.as.oss.http.api.proto.HttpDownloadRequest;
import com.google.android.as.oss.http.api.proto.HttpProperty;
import com.google.android.as.oss.http.api.proto.ResponseHeaders;
import com.google.android.as.oss.http.api.proto.UnrecognizedUrlException;
import com.google.android.as.oss.http.client.HttpDownloaderCallback;
import com.google.android.as.oss.http.client.HttpDownloaderClient;
import com.google.android.as.oss.http.config.PcsHttpConfig;
import com.google.android.libraries.net.downloader.Downloader.NonRetriableTransportError;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.google.rpc.Status;
import java.io.FileDescriptor;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/** Pcs's implementation of {@link HttpURLConnection}. */
public class PcsHttpConnection extends HttpURLConnection {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final ConfigReader<PcsHttpConfig> configReader;
  private final HttpDownloaderClient client;
  private final AtomicReference<IOException> ioExceptionRef = new AtomicReference<>();

  private volatile @MonotonicNonNull ResponseHeaders responseHeaders;
  private volatile @MonotonicNonNull InputStream is;
  private volatile @MonotonicNonNull OutputStream os;
  @Nullable private volatile ParcelFileDescriptor[] pipe;

  @GuardedBy("this")
  private @MonotonicNonNull ListenableFuture<Void> downloadFuture;

  private final AtomicBoolean disconnected = new AtomicBoolean(false);
  @Nullable private FileDescriptor readFd = null;
  private boolean isReadTimeoutSet = false;

  // This latch fixes a race that can occur when the network connect is dropped on the Pcs
  // side. The latch ensures the client waits for a message from binder service such as onError
  // before it can finish consuming the stream.
  private final CountDownLatch pendingPfdLatch = new CountDownLatch(1);

  // This latch ensures the connect method doesn't return until after the headers have
  // been retrieved.
  private final CountDownLatch pendingHeadersLatch = new CountDownLatch(1);

  private static final ImmutableSet<String> HEADER_ALLOW_LIST = ImmutableSet.of("content-length");

  PcsHttpConnection(
      HttpDownloaderClient client, URL u, ConfigReader<PcsHttpConfig> httpConfigReader) {
    super(u);
    this.client = client;
    this.configReader = httpConfigReader;
  }

  @Override
  public void setReadTimeout(int timeout) {
    super.setReadTimeout(timeout);
    isReadTimeoutSet = true;
  }

  @Override
  public int getReadTimeout() {
    return isReadTimeoutSet
        ? super.getReadTimeout()
        : (int) configReader.getConfig().defaultReadTimeout().toMillis();
  }

  @Override
  public void disconnect() {
    logger.atFine().log("PcsHttpConnection#disconnect [%s]", url);
    if (disconnected.getAndSet(true)) {
      return;
    }
    pendingHeadersLatch.countDown();
    cancelDownload();
    // Add cleanup calls for robustness
    tryCloseOutputPipe();
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        logger.atWarning().withCause(e).log(
            "Error closing input stream on disconnect (url=[%s]).", url);
      }
    }
  }

  private synchronized void cancelDownload() {
    if (downloadFuture != null) {
      downloadFuture.cancel(/* mayInterruptIfRunning= */ true);
    }
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public int getResponseCode() throws IOException {
    if (responseHeaders == null) {
      return -1;
    }
    return responseHeaders.getResponseCode();
  }

  /**
   * Returns the first value set for the requested header name, if one exists, and if the named
   * header is in the set of headers that can be read. Otherwise returns {@code null}.
   */
  @Nullable
  @Override
  public String getHeaderField(String name) {
    String normalizedName = Ascii.toLowerCase(name);
    if (!HEADER_ALLOW_LIST.contains(normalizedName)) {
      return null;
    }

    int headerCount = responseHeaders.getHeaderCount();
    for (int i = 0; i < headerCount; i++) {
      HttpProperty header = responseHeaders.getHeader(i);
      if (header.getValueCount() > 0 && Ascii.equalsIgnoreCase(header.getKey(), normalizedName)) {
        return header.getValue(0);
      }
    }
    return null;
  }

  @Override
  public synchronized void connect() throws IOException {
    logger.atFine().log("PcsHttpConnection#connect");

    if (disconnected.get()) {
      throw new IOException("Already disconnected");
    }

    HttpDownloadRequest.Builder request = HttpDownloadRequest.newBuilder();
    request.setUrl(getURL().toString());
    for (Map.Entry<String, List<String>> requestProperty : getRequestProperties().entrySet()) {
      request.addRequestProperty(
          HttpProperty.newBuilder()
              .setKey(requestProperty.getKey())
              .addAllValue(requestProperty.getValue())
              .build());
    }

    pipe = configReader.getConfig().writeToPfd() ? ParcelFileDescriptor.createReliablePipe() : null;
    if (pipe != null) {
      is = new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]);
      readFd = pipe[0].getFileDescriptor();
    } else {
      os = new PipedOutputStream();
      is = new PipedInputStream((PipedOutputStream) os);
      pendingPfdLatch.countDown(); // PFD is not enabled so unblock latch now.
    }

    downloadFuture =
        client.download(
            request.build(),
            pipe == null ? null : pipe[1],
            new HttpDownloaderCallback() {
              private long downloadSize = 0;

              @Override
              public void onDownloadStarted(ResponseHeaders headers) {
                logger.atInfo().log("Started download from url='%s'", url);
                PcsHttpConnection.this.responseHeaders = headers;
                pendingHeadersLatch.countDown();
              }

              @Override
              public void onDownloadedChunk(byte[] bytes) {
                pendingHeadersLatch.countDown();

                if (os == null && pipe == null) {
                  onClientError(new IllegalStateException("Invalid state for file download."));
                  return;
                } else if (os == null && pipe != null) {
                  logger.atWarning().log(
                      "Using streaming download since service doesn't seem to support direct write"
                          + " to disk.");
                  os = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]);
                }

                try {
                  os.write(bytes);
                  os.flush();
                  downloadSize += bytes.length;
                  logger.atFine().log(
                      "HTTP client received chunk of download [%d bytes, Total so far: %d bytes].",
                      bytes.length, downloadSize);
                } catch (IOException e) {
                  onClientError(e);
                }
              }

              @Override
              public void onDownloadComplete() {
                if (os == null) {
                  logger.atInfo().log(
                      "DOWNLOAD COMPLETE: Downloaded unknown number of bytes from %s.", url);
                } else {
                  logger.atInfo().log(
                      "DOWNLOAD COMPLETE: Downloaded %d bytes from %s.", downloadSize, url);
                }
                pendingHeadersLatch.countDown(); // Presumably was already counted down.
                tryCloseOutputPipe();
              }

              @Override
              public void onError(Throwable t) {
                logger.atSevere().withCause(t).log(
                    "Service error while downloading from url='%s'", url);

                Optional<Status> status = GrpcStatusProto.fromThrowable(t);
                if (status.isPresent()) {
                  Optional<UnrecognizedUrlException> unrecognizedUrlException =
                      GrpcStatusProto.findDetail(
                          status.get(), UnrecognizedUrlException.getDefaultInstance());
                  if (unrecognizedUrlException.isPresent()) {
                    logger.atFine().log(
                        "Found unrecognized url exception - setting failure as non-retriable.");
                    ioExceptionRef.set(
                        new NonRetriableTransportError(
                            String.format("Connection failed for url=[%s]", url), t));
                  }
                }

                ioExceptionRef.compareAndSet(
                    null, (t instanceof IOException) ? (IOException) t : new IOException(t));
                // NOTE: onError can be called without onDownloadStarted.
                pendingHeadersLatch.countDown();
                tryCloseOutputPipe();
              }

              private void onClientError(Throwable t) {
                logger.atSevere().withCause(t).log(
                    "Client-side error while downloading from url='%s'", url);
                tryCloseOutputPipe();
              }
            });

    try {
      logger.atFine().log("awaiting headers");
      // Note that using the connectTimeout is not entirely accurate, since we are waiting for
      // headers to be returned, rather than just the tcp handshake to be successful.
      if (getConnectTimeout() == 0) {
        pendingHeadersLatch.await();
      } else {
        if (!pendingHeadersLatch.await(getConnectTimeout(), MILLISECONDS)) {
          tryCloseOutputPipe();
          throw new IOException(String.format("Timed out waiting for response for url=[%s]", url));
        }
      }
      logger.atFine().log("awaiting headers done");
    } catch (InterruptedException e) {
      tryCloseOutputPipe();
      Thread.currentThread().interrupt();
      throw new IOException(String.format("Interrupted waiting for response: url=[%s]", url), e);
    }
    maybeThrowIoException();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FilterInputStream(is) {
      @Override
      public int read() throws IOException {
        maybeThrowIoException();
        if (in.available() == 0) {
          checkReadTimeout();
        }
        return processReadResult(in.read());
      }

      @Override
      public int read(byte[] b) throws IOException {
        maybeThrowIoException();
        if (in.available() == 0) {
          checkReadTimeout();
        }
        return processReadResult(in.read(b));
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        maybeThrowIoException();
        if (in.available() == 0) {
          checkReadTimeout();
        }
        return processReadResult(in.read(b, off, len));
      }

      @Override
      public void close() throws IOException {
        in.close();
      }
    };
  }

  @VisibleForTesting
  @Nullable
  public OutputStream getOutputStreamForTesting() throws IOException {
    if (os != null) {
      return os;
    } else if (pipe != null) {
      os = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]);
      return os;
    }
    return null;
  }

  @VisibleForTesting
  synchronized void waitForDownload() throws InterruptedException, ExecutionException {
    downloadFuture.get();
  }

  private void maybeThrowIoException() throws IOException {
    if (disconnected.get()) {
      // No need to propagate error if client disconnected already.
      return;
    }
    IOException e = ioExceptionRef.get();
    if (e != null) {
      throw e;
    }
  }

  /**
   * Validates that data is available to read within the configured read timeout window.
   *
   * <p>If a native parcel file descriptor (PFD) is present, uses {@link Os#poll(StructPollfd[],
   * int)} to wait for incoming bytes at the OS level without CPU busy-waiting.
   *
   * @throws SocketTimeoutException if the timeout expires before data becomes available.
   * @throws InterruptedIOException if the waiting thread is interrupted.
   * @throws IOException if an I/O error occurred on the underlying connection or poll failed.
   */
  private void checkReadTimeout() throws IOException {
    if (readFd != null) {
      checkReadTimeoutFd();
    }
  }

  private void checkReadTimeoutFd() throws IOException {
    if (pendingPfdLatch.getCount() == 0) {
      return;
    }

    int timeoutMs = getReadTimeout();
    if (timeoutMs <= 0) {
      return;
    }

    StructPollfd pollfd = new StructPollfd();
    pollfd.fd = readFd;
    pollfd.events = (short) OsConstants.POLLIN;

    try {
      StructPollfd[] pollFds = new StructPollfd[] {pollfd};
      int readyCount;
      StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
      try {
        StrictMode.setThreadPolicy(
            new StrictMode.ThreadPolicy.Builder(oldPolicy)
                .permitNetwork()
                .permitUnbufferedIo()
                .build());
        readyCount = Os.poll(pollFds, timeoutMs);
      } finally {
        StrictMode.setThreadPolicy(oldPolicy);
      }

      if (readyCount == 0) {
        maybeThrowIoException();
        cancelDownload();
        tryCloseOutputPipe();
        throw new SocketTimeoutException(
            String.format("Read timed out waiting for data: url=[%s]", url));
      }
    } catch (ErrnoException e) {
      if (e.errno == OsConstants.EINTR) {
        if (Thread.currentThread().isInterrupted()) {
          throw new InterruptedIOException("Interrupted waiting for data");
        }
      }
      throw new IOException("Poll failed while waiting for data", e);
    }
    maybeThrowIoException();
  }

  private void waitForTerminalCallback() {
    int timeoutMs = getReadTimeout();
    // Safeguard against indefinite lock if timeout is fully disabled (<= 0).
    // The executor callback latency should be sub-millisecond, so 5000ms is highly conservative.
    if (timeoutMs <= 0) {
      timeoutMs = 5000;
    }
    try {
      pendingPfdLatch.await(timeoutMs, MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @CanIgnoreReturnValue
  private int processReadResult(int bytesRead) throws IOException {
    // -1 indicates End Of File (EOF) or EOF-like termination from the downstream components.
    // When EOF is reached, the internal pipe relies on gRPC async callbacks (onCompleted, onError).
    // We wait for the terminal callback to confirm that pending exceptions are propagated before
    // returning.
    // If we don't wait, we might return EOF (-1) before the exception is thrown wrongly signaling
    // a successful read completion.
    if (bytesRead == -1) {
      waitForTerminalCallback();
    }
    maybeThrowIoException();
    return bytesRead;
  }

  private void tryCloseOutputPipe() {
    pendingPfdLatch.countDown();
    if (os != null) {
      try {
        // If os was created, closing it will also close the underlying PFD.
        os.close();
      } catch (IOException e) {
        logger.atWarning().withCause(e).log(
            "Error while closing output stream for download (url=[%s]).", url);
      }
    } else if (pipe != null && pipe[1] != null) {
      // This handles the case where os was never initialized.
      // We must close the PFD directly to prevent a leak.
      try {
        pipe[1].close();
      } catch (IOException e) {
        logger.atWarning().withCause(e).log(
            "Error while closing output PFD for download (url=[%s]).", url);
      }
    }
  }
}
