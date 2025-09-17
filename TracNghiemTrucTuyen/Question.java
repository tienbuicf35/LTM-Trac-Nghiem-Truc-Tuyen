package Quiz;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    private String questionText;
    private List<String> options;
    private int correct; // chỉ số đáp án đúng (0-3)

    public Question(String questionText, List<String> options, int correct) {
        this.questionText = questionText;
        this.options = options;
        this.correct = correct;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrect() {
        return correct;
    }
}