package com.example.mom.data.repository

import com.example.mom.data.remote.ViolationRemoteDataSource
import com.example.mom.domain.model.ActiveViolation
import com.example.mom.domain.model.ViolationLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ViolationRepositoryImplTest {
    private lateinit var fakeRemote: FakeViolationRemoteDataSource
    private lateinit var repository: ViolationRepositoryImpl

  @BeforeEach
  fun setUp() {
    fakeRemote = FakeViolationRemoteDataSource()
    repository = ViolationRepositoryImpl(fakeRemote)
  }

    @Test
    fun `given no active violation when recordIfEntered then creates log and active`() =
        runTest {
            val log = sampleLog(occurredAt = 1_000L)

            repository.recordIfEntered(log)

            assertEquals(1, fakeRemote.logs.size)
            assertNotNull(fakeRemote.active[log.interestId])
        }

    @Test
    fun `given active violation exists when recordIfEntered again then does not duplicate log`() =
        runTest {
            val first = sampleLog(occurredAt = 1_000L)
            repository.recordIfEntered(first)
            val countAfterFirst = fakeRemote.logs.size

            repository.recordIfEntered(sampleLog(occurredAt = 2_000L))

            assertEquals(countAfterFirst, fakeRemote.logs.size)
        }

    @Test
    fun `given active cleared by markInside when recordIfEntered again then adds one new log`() =
        runTest {
            val interestId = 42L
            repository.recordIfEntered(sampleLog(interestId = interestId, occurredAt = 1_000L))
            repository.markInside(interestId)
            assertNull(fakeRemote.active[interestId])

            repository.recordIfEntered(sampleLog(interestId = interestId, occurredAt = 2_000L))

            assertEquals(2, fakeRemote.logs.size)
            assertNotNull(fakeRemote.active[interestId])
        }

    private fun sampleLog(
        interestId: Long = 42L,
        occurredAt: Long = 1_000L,
    ): ViolationLog =
        ViolationLog(
            id = 0,
            childId = 7L,
            childName = "Kid",
            interestId = interestId,
            interestName = "School",
            occurredAt = occurredAt,
            measuredLatitude = 37.57,
            measuredLongitude = 126.98,
            distanceKm = 2.0,
        )

    private class FakeViolationRemoteDataSource : ViolationRemoteDataSource {
        val logs = mutableListOf<ViolationLog>()
        val active = mutableMapOf<Long, ActiveViolation>()

        override fun observeViolations(): Flow<List<ViolationLog>> = flowOf(logs.toList())

        override suspend fun findActive(interestId: Long): ActiveViolation? =
            active[interestId]

        override suspend fun recordViolation(log: ViolationLog): ViolationLog {
            val stored = log.copy(id = logs.size.toLong() + 1)
            logs.add(stored)
            active[log.interestId] =
                ActiveViolation(
                    interestId = log.interestId,
                    childId = log.childId,
                    startedAt = log.occurredAt,
                )
            return stored
        }

        override suspend fun clearActive(interestId: Long) {
            active.remove(interestId)
        }
    }
}