package tech.diadochi.repo.pagination

final case class Pagination(limit: Int, offset: Int)

object Pagination {

  val defaultPageSize = 10

  def apply(maybeLimit: Option[Int], maybeOffset: Option[Int]) =
    new Pagination(maybeLimit.getOrElse(defaultPageSize), maybeOffset.getOrElse(0))

}
