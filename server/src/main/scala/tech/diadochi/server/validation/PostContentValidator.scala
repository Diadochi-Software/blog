package tech.diadochi.server.validation

import cats.data.Validated.*
import cats.implicits.*
import cats.syntax.all.*
import tech.diadochi.core.PostContent
import tech.diadochi.server.validation.ValidationFailure.InvalidLanguageFormat
import tech.diadochi.server.validation.Validator.{ValidationResult, validateRequired}

class PostContentValidator extends Validator[PostContent] {

  private def validateIsIso639_1(language: String): ValidationResult[String] =
    if (language.matches("[a-z]{2}")) language.validNel
    else InvalidLanguageFormat(language).invalidNel

  private def validateLanguage(
      language: String
  ): ValidationResult[String] =
    validateRequired(language, "language")(_.isEmpty) andThen validateIsIso639_1

  override def validate(postContent: PostContent): ValidationResult[PostContent] = {
    val PostContent(
      postId,
      language,
      title,
      description,
      content
    ) = postContent

    val languageValidation = validateLanguage(language)
    val titleValidation    = validateRequired(title, "title")(_.isEmpty)
    val descriptionValidation =
      validateRequired(description, "description")(_.isEmpty)
    val contentValidation = validateRequired(content, "content")(_.isEmpty)

    (languageValidation, titleValidation, descriptionValidation, contentValidation)
      .mapN(PostContent(postId, _, _, _, _))

  }

}
