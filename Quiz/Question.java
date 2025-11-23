package Quiz;

import java.io.Serializable;
import java.util.List;

// Lớp này phải implement Serializable vì nó được gửi qua ObjectInputStream/ObjectOutputStream
public class Question implements Serializable {
    // Để đảm bảo tương thích khi deserialize
    private static final long serialVersionUID = 1L; 
    
    private String questionText;
    private List<String> options;
    private int correctAnswer; // Index của đáp án đúng (0, 1, 2, hoặc 3)

    // Constructor 
    public Question(String questionText, List<String> options, int correctAnswer) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters được sử dụng trong Quizclient
    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }
    
    public int getCorrectAnswer() {
        return correctAnswer;
    }
}
