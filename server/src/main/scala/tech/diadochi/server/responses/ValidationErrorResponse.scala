package tech.diadochi.server.responses

import tech.diadochi.server.validation.ValidationFailure

case class ValidationErrorResponse(errors: List[ValidationFailure])
