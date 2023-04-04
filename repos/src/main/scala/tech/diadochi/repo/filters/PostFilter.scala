package tech.diadochi.repo.filters

import java.time.LocalDateTime

case class PostFilter(
    authors: List[String] = List.empty[String],
    languages: List[String] = List.empty[String],
    tags: List[String] = List.empty[String],
    maybeLapse: Option[(LocalDateTime, LocalDateTime)] = None
)
