package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: CreateBudgetRequest): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorEntity.findById(it) }
            }
            entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
// Базовый фильтр по году
            var condition = BudgetTable.year eq param.year

            // Если указан фильтр по ФИО автора
            if (!param.authorName.isNullOrBlank()) {
                // INNER JOIN с author и фильтр по подстроке (игнорируя регистр)
                val pattern = "%${param.authorName.toLowerCase()}%"
                condition = condition and (BudgetTable.author neq null)
                // Применяем join и фильтр по author.full_name
                val joinedQuery = BudgetTable.innerJoin(AuthorTable)
                    .select { condition and (AuthorTable.fullName.lowerCase() like pattern) }
                val total = joinedQuery.count()
                val allData = BudgetEntity.wrapRows(joinedQuery).map { it.toResponse() }
                val sumByType = allData.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

                val pagedQuery = BudgetTable.innerJoin(AuthorTable)
                    .select { condition and (AuthorTable.fullName.lowerCase() like pattern) }
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                    .limit(param.limit, param.offset)
                val data = BudgetEntity.wrapRows(pagedQuery).map { it.toResponse() }

                return@transaction BudgetYearStatsResponse(
                    total = total,
                    totalByType = sumByType,
                    items = data
                )
            } else {
                // Старый вариант без фильтра по автору

                // 1. Получаем все записи за год (для статистики)
                val allQuery = BudgetTable.select { BudgetTable.year eq param.year }
                val total = allQuery.count()
                val allData = BudgetEntity.wrapRows(allQuery).map { it.toResponse() }
                val sumByType = allData.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

                // 2. Пагинируем и сортируем
                val pagedQuery = BudgetTable
                    .select { BudgetTable.year eq param.year }
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                    .limit(param.limit, param.offset)
                val data = BudgetEntity.wrapRows(pagedQuery).map { it.toResponse() }

                return@transaction BudgetYearStatsResponse(
                    total = total,
                    totalByType = sumByType,
                    items = data
                )
            }
        }
    }
}