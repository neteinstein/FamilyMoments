package org.neteinstein.family.data.source

import org.neteinstein.family.domain.model.Question
import org.neteinstein.family.domain.model.QuestionCategory

object QuestionDataSource {

    private val englishQuestions = listOf(
        Question(id = 1, text = "If you could have dinner with anyone in history, who would it be and why?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 2, text = "What is your happiest childhood memory?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 3, text = "If you could travel anywhere in the world, where would you go?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 4, text = "What is one skill you wish you had?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 5, text = "If you could change one thing about the world, what would it be?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 6, text = "What is the most important lesson life has taught you?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 7, text = "What three words would your closest friends use to describe you?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 8, text = "If you had unlimited money for one day, how would you spend it?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 9, text = "What is your favourite family tradition?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 10, text = "What achievement are you most proud of?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 11, text = "What is something you have always wanted to learn?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 12, text = "If you could live in any era, which would you choose?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 13, text = "What is your favourite memory of us together?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 14, text = "What makes you feel most loved?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 15, text = "If you could give your younger self one piece of advice, what would it be?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 16, text = "What activity in your day-to-day life brings you the most joy?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 17, text = "What is the best book you have ever read?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 18, text = "If you could meet any fictional character, who would it be?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 19, text = "What would be your dream job if money was not a factor?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 20, text = "What is one thing you want to accomplish in the next year?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 21, text = "What does a perfect day look like for you?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 22, text = "Which person in your life has influenced you the most, and how?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 23, text = "If you could master any talent overnight, what would it be?", languageCode = "en", category = QuestionCategory.Dreams),
        Question(id = 24, text = "What is something you are grateful for today?", languageCode = "en", category = QuestionCategory.Reflections),
        Question(id = 25, text = "What is a fear you have overcome, and how did you do it?", languageCode = "en", category = QuestionCategory.Reflections),
    )

    private val portugueseQuestions = listOf(
        Question(id = 101, text = "Se pudesses jantar com qualquer pessoa da história, quem seria e porquê?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 102, text = "Qual é a tua memória de infância mais feliz?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 103, text = "Se pudesses viajar para qualquer lugar do mundo, para onde irias?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 104, text = "Que competência gostavas de ter?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 105, text = "Se pudesses mudar uma coisa no mundo, o que seria?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 106, text = "Qual é a lição mais importante que a vida te ensinou?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 107, text = "Que três palavras os teus amigos usariam para te descrever?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 108, text = "Se tivesses dinheiro ilimitado por um dia, como o gastarias?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 109, text = "Qual é a tua tradição familiar favorita?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 110, text = "De que conquista te orgulhas mais?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 111, text = "Qual é algo que sempre quiseste aprender?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 112, text = "Se pudesses viver em qualquer época, qual seria?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 113, text = "Qual é a tua memória favorita de nós juntos?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 114, text = "O que te faz sentir mais amado(a)?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 115, text = "Que conselho darias ao teu eu mais jovem?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 116, text = "Que atividade do dia-a-dia te traz mais alegria?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 117, text = "Qual é o melhor livro que já leste?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 118, text = "Se pudesses conhecer qualquer personagem fictício, quem seria?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 119, text = "Qual seria o teu trabalho de sonho se o dinheiro não fosse problema?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 120, text = "O que queres alcançar no próximo ano?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 121, text = "Como é o teu dia perfeito?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 122, text = "Quem na tua vida te influenciou mais e de que forma?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 123, text = "Se pudesses dominar qualquer talento de um dia para o outro, qual seria?", languageCode = "pt", category = QuestionCategory.Dreams),
        Question(id = 124, text = "Qual é algo pelo qual és grato(a) hoje?", languageCode = "pt", category = QuestionCategory.Reflections),
        Question(id = 125, text = "Qual é um medo que superaste e como o fizeste?", languageCode = "pt", category = QuestionCategory.Reflections),
    )

    fun getQuestions(languageCode: String): List<Question> =
        when (languageCode) {
            "pt" -> portugueseQuestions
            else -> englishQuestions
        }
}
