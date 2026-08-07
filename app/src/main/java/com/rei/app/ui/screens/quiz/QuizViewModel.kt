package com.rei.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rei.app.data.repository.AnimeRepository
import com.rei.app.domain.model.Anime
import com.rei.app.economy.EconomyManager
import com.rei.app.economy.ReiRewards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repo: AnimeRepository,
    private val economy: EconomyManager
) : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private var pool = mutableListOf<Anime>()
    private var questions = mutableListOf<QuizQuestionData>()
    private var correctCount = 0

    fun start() = viewModelScope.launch {
        _state.value = QuizState(isLoading = true)
        correctCount = 0
        try {
            // Load anime pool from multiple sources
            val trending = mutableListOf<Anime>()
            repo.getTrending().collect { trending.addAll(it.first) }
            val popular = mutableListOf<Anime>()
            repo.getPopularThisSeason().collect { popular.addAll(it.first) }
            pool = (trending + popular).distinctBy { it.id }.filter { it.coverImage.best != null }.toMutableList()
            if (pool.size < 4) { _state.value = QuizState(); return@launch }
            pool.shuffle()
            generateQuestions()
            _state.value = QuizState(question = questions.getOrNull(0), currentQuestion = 1, total = minOf(10, questions.size))
        } catch (_: Exception) { _state.value = QuizState() }
    }

    private fun generateQuestions() {
        questions.clear()
        val count = minOf(10, pool.size / 4)
        for (i in 0 until count) {
            val correct = pool[i]
            val wrongs = pool.filter { it.id != correct.id }.shuffled().take(3)
            if (wrongs.size < 3) continue
            val options = (listOf(correct) + wrongs).shuffled()
            questions.add(QuizQuestionData(
                coverUrl = correct.coverImage.best ?: continue,
                correctAnswer = correct.title.primary,
                options = options.map { it.title.primary },
                correctIndex = options.indexOf(correct)
            ))
        }
    }

    fun answer(index: Int) {
        val s = _state.value
        val q = s.question ?: return
        val isCorrect = index == q.correctIndex
        if (isCorrect) correctCount++
        _state.value = s.copy(
            question = q.copy(selectedAnswer = index),
            score = s.score + if (isCorrect) 1 else 0
        )
        // Award coins for correct answer in real-time
        if (isCorrect) {
            viewModelScope.launch {
                economy.earn(2, "quiz_correct", "quiz_q${s.currentQuestion}")
            }
        }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val next = s.currentQuestion
            if (next >= s.total) {
                _state.value = _state.value.copy(question = null, isFinished = true)
            } else {
                _state.value = _state.value.copy(question = questions.getOrNull(next), currentQuestion = next + 1)
            }
        }
    }

    fun restart() { _state.value = QuizState(); correctCount = 0; start() }
}
