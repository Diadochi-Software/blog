package tech.diadochi.server.validation

import cats.*
import cats.data.Validated.*
import cats.implicits.*
import tech.diadochi.core.PostContent

import java.util.UUID

object Validators {

  given postContentValidator: Validator[PostContent] = new PostContentValidator

}
