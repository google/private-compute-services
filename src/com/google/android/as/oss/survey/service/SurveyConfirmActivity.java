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

package com.google.android.as.oss.survey.service;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.joining;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService;
import com.google.android.as.oss.survey.api.proto.Event.QuestionAnswered;
import com.google.android.as.oss.survey.api.proto.Event.QuestionAnswered.Selection;
import com.google.android.as.oss.survey.api.proto.HttpSurveyRecordEventRequest;
import com.google.android.as.oss.survey.api.proto.HttpSurveyRecordEventRequestList;
import com.google.android.as.oss.survey.api.proto.HttpSurveyResponse;
import com.google.android.as.oss.survey.api.proto.HttpUploadSurveyRequest;
import com.google.android.as.oss.survey.api.proto.SurveyQuestion;
import com.google.android.material.card.MaterialCardView;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.ExtensionRegistryLite;
import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

/** Activity for display survey confirmation dialog. */
@AndroidEntryPoint(AppCompatActivity.class)
public final class SurveyConfirmActivity extends Hilt_SurveyConfirmActivity {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  static final String INTENT_EXTRA_SURVEY_REQUEST = "requests";
  private Map<String, String> surveyResponsesMap;
  private HttpUploadSurveyRequest uploadSurveyRequest;
  private ImageButton closeButton;
  private Button submitButton;
  private Button cancelButton;
  @Inject @GrpcService SurveyGrpcBindableService surveyGrpcBindableService;

  @Override
  @SuppressLint("SetTextI18n")
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {

      byte[] surveyRequest = getIntent().getByteArrayExtra(INTENT_EXTRA_SURVEY_REQUEST);

      if (surveyRequest == null) {
        logger.atWarning().log("No survey request in this activity");
        finish();
        return;
      }

      uploadSurveyRequest =
          HttpUploadSurveyRequest.parseFrom(
              surveyRequest, ExtensionRegistryLite.getGeneratedRegistry());

      surveyResponsesMap = getSurveyResponsesMap(uploadSurveyRequest);

    } catch (Exception e) {
      logger.atWarning().withCause(e).log("Parse survey requests failed");
      finish();
      return;
    }

    // Set the transparent background.
    this.setTranslucent(true);
    this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

    displayConfirmationDialog();
  }

  @Override
  protected void onPause() {
    if (isFinishing()) {
      overridePendingTransition(0, 0);
    }
    super.onPause();
  }

  @Override
  protected void onStart() {
    overridePendingTransition(0, 0);
    super.onStart();
  }

  private void displayConfirmationDialog() {
    if (surveyResponsesMap == null || surveyResponsesMap.isEmpty()) {
      logger.atWarning().log("No available survey response in this survey");
      finish();
      return;
    }

    setContentView(R.layout.survey_confirm_dialog);
    MaterialCardView cardView = findViewById(R.id.survey_prompt_container);
    updateCardViewWidth(cardView);

    updatePromptBannerText(R.id.survey_prompt_title_text);
    updatePromptBannerLogo(R.drawable.google_g_logo, R.id.survey_prompt_banner_logo);

    ViewGroup promptBannerContainer = findViewById(R.id.survey_prompt_banner_container);
    updateConfirmDialogContent(getLayoutInflater(), promptBannerContainer);
    setupButtons(this);
  }

  private void updateConfirmDialogContent(LayoutInflater layoutInflater, ViewGroup container) {
    surveyResponsesMap.entrySet().stream()
        .map(
            entry -> {
              View view = layoutInflater.inflate(R.layout.survey_confirm_item, container, false);
              TextView questionText = view.findViewById(R.id.survey_confirm_question_text);
              TextView answerText = view.findViewById(R.id.survey_confirm_answer_text);
              questionText.setText(entry.getKey());
              answerText.setText(entry.getValue());
              container.addView(view);
              return view;
            })
        .forEach(view -> view.setVisibility(View.VISIBLE));
  }

  /**
   * Sets and wraps the prompt banner text to two lines and shrinks it if the text is too long. Also
   * switches to the two line prompt accordingly.
   */
  private void updatePromptBannerText(int surveyConfirmTitleId) {
    TextView promptTextView = findViewById(surveyConfirmTitleId);
    Spanned promptMessageFromHtml =
        HtmlCompat.fromHtml(promptTextView.getText().toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
    promptTextView.setText(promptMessageFromHtml);
    promptTextView.announceForAccessibility(promptMessageFromHtml.toString());
  }

  /**
   * Updates Logo image in the prompt banner. If there is no logo to show then removes the image
   * view from the container.
   *
   * @param surveyLogoDrawable the resource ID for the logo or null if no logo should be shown
   * @param surveyLogoId the image view ID for the logo
   */
  private void updatePromptBannerLogo(@DrawableRes int surveyLogoDrawable, int surveyLogoId) {
    ImageView imageView = findViewById(surveyLogoId);
    updateImageViewWithLogo(imageView, surveyLogoDrawable);
  }

  private void updateCardViewWidth(MaterialCardView cardView) {
    LayoutParams params = cardView.getLayoutParams();
    if (params != null) {
      params.width = getSurveyContainerMaxWidth(cardView.getContext());
      cardView.setLayoutParams(params);
    }
  }

  /**
   * Sets the content of the {@code imageView} to the drawable corresponding to {@code logoResId} if
   * it's not null or 0. Otherwise, the {@code imageView} is hidden.
   *
   * @param imageView the {@link ImageView} for displaying the logo
   * @param surveyLogoDrawable the resource ID of the logo or null if no logo should be shown
   */
  private void updateImageViewWithLogo(ImageView imageView, @DrawableRes int surveyLogoDrawable) {
    if (surveyLogoDrawable != 0) {
      imageView.setImageResource(surveyLogoDrawable);
      imageView.setVisibility(View.VISIBLE);
    } else {
      imageView.setVisibility(View.GONE);
    }
  }

  private int getSurveyContainerMaxWidth(Context context) {
    Configuration config = context.getResources().getConfiguration();
    if (config.smallestScreenWidthDp >= 411) {
      return getPixelOffset(context, R.dimen.survey_prompt_max_width_sw_411);
    }

    if (config.smallestScreenWidthDp >= 380) {
      return getPixelOffset(context, R.dimen.survey_prompt_max_width_sw_380);
    }

    if (config.smallestScreenWidthDp >= 320) {
      return getPixelOffset(context, R.dimen.survey_prompt_max_width_sw_320);
    }

    if (config.smallestScreenWidthDp >= 240) {
      return getPixelOffset(context, R.dimen.survey_prompt_max_width_sw_240);
    }

    return getPixelOffset(context, R.dimen.survey_prompt_max_width);
  }

  private int getPixelOffset(Context context, int dimen) {
    return context.getResources().getDimensionPixelOffset(dimen);
  }

  /**
   * Returns a version of the {@link Drawable} recolored to a given color.
   *
   * @param drawableResId the resource ID for the {@link Drawable} to be recolored.
   * @param context the {@link Context} to obtain application resources.
   * @param colorResId the resource ID for the color.
   */
  @Nullable
  private Drawable getRecoloredDrawable(
      @DrawableRes int drawableResId, Context context, int colorResId) {
    Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
    if (drawable == null) {
      return null;
    }
    int color = ContextCompat.getColor(context, colorResId);
    return getRecoloredDrawable(drawable, context, color);
  }

  /**
   * Returns a version of the {@link Drawable} recolored to a given color.
   *
   * @param drawable the {@link Drawable} to be recolored.
   * @param context the {@link Context} to obtain application resources.
   * @param color the color.
   */
  @Nullable
  private Drawable getRecoloredDrawable(Drawable drawable, Context context, int color) {
    ConstantState constantState = drawable.getConstantState();
    if (constantState == null) {
      return null;
    }
    Drawable mutableDrawableCopy = constantState.newDrawable(context.getResources()).mutate();
    // Use Mode.SRC_ATOP to make the entire (dest) icon get colored by the (source) color.
    // Check http://ssp.impulsetrain.com/porterduff.html for a visual explanation of the modes.
    mutableDrawableCopy.setColorFilter(color, Mode.SRC_ATOP);
    return mutableDrawableCopy;
  }

  private void setupButtons(Context themeContext) {
    closeButton = findViewById(R.id.survey_close_button);
    closeButton.setImageDrawable(
        getRecoloredDrawable(
            R.drawable.survey_close_button_icon, themeContext, R.color.survey_close_icon_color));
    setOnClickListenerForButton(closeButton, view -> cancelSurvey());

    cancelButton = findViewById(R.id.survey_confirm_cancel_button);
    setOnClickListenerForButton(cancelButton, view -> cancelSurvey());

    submitButton = findViewById(R.id.survey_confirm_submit_button);
    setOnClickListenerForButton(submitButton, view -> uploadSurvey());
  }

  private void setOnClickListenerForButton(View button, OnClickListener listener) {
    if (button == null) {
      return;
    }
    button.setOnClickListener(listener);
  }

  private void removeButtonListeners() {
    if (submitButton == null || cancelButton == null || closeButton == null) {
      return;
    }
    submitButton.setOnClickListener(null);
    cancelButton.setOnClickListener(null);
    closeButton.setOnClickListener(null);
  }

  private void uploadSurvey() {
    removeButtonListeners();
    surveyGrpcBindableService.uploadSurvey(
        uploadSurveyRequest.getSurveyTriggerId(),
        uploadSurveyRequest.getRequestList(),
        new ClientResponseObserver<HttpSurveyRecordEventRequestList, HttpSurveyResponse>() {
          @Override
          public void beforeStart(
              ClientCallStreamObserver<HttpSurveyRecordEventRequestList> requestStream) {}

          @Override
          public void onNext(HttpSurveyResponse surveyResponse) {}

          @Override
          public void onError(Throwable t) {
            finish();
          }

          @Override
          public void onCompleted() {
            setResult(RESULT_OK);
            finish();
          }
        });
  }

  private void cancelSurvey() {
    removeButtonListeners();
    setResult(RESULT_CANCELED);
    finish();
  }

  private Map<String, String> getSurveyResponsesMap(HttpUploadSurveyRequest uploadSurveyRequest) {
    ImmutableMap<String, SurveyQuestion> questionsMap =
        uploadSurveyRequest.getQuestionsList().stream()
            .collect(toImmutableMap(SurveyQuestion::getQuestionOrdinal, Function.identity()));

    Map<String, String> responseMap = new LinkedHashMap<>();
    for (HttpSurveyRecordEventRequest request :
        uploadSurveyRequest.getRequestList().getRequestsList()) {
      if (!request.getEvent().hasQuestionAnswered()) {
        continue;
      }

      QuestionAnswered answer = request.getEvent().getQuestionAnswered();

      SurveyQuestion question = questionsMap.get(String.valueOf(answer.getQuestionOrdinal()));
      if (question == null) {
        throw new IllegalArgumentException("Question should not be null");
      }
      String questionText = question.getQuestionText();
      if (answer.hasSingleSelection()) {
        responseMap.put(questionText, answer.getSingleSelection().getAnswer().getText());
      } else if (answer.hasRating()) {
        responseMap.put(questionText, answer.getRating().getAnswer().getText());
      } else if (answer.hasMultipleSelection()) {
        String result =
            answer.getMultipleSelection().getAnswerList().stream()
                .map(Selection::getText)
                .collect(joining(","));

        String questionSuffixText =
            question.getQuestionSuffixText().isEmpty() ? "" : question.getQuestionSuffixText();

        responseMap.put(questionText, result + questionSuffixText);
      }
    }
    return responseMap;
  }
}
