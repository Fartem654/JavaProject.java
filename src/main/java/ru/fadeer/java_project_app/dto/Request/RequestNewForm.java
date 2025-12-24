package ru.fadeer.java_project_app.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Request;

/// Класс-форма для промежуточного хранения новой заявки
@Getter
@Setter
public class RequestNewForm {

	private Integer requestId;

	@NotBlank(message = "Тип аварии обязателен.")
	private String accidentType;

	@NotNull(message = "Приоритет обязателен")
	private Request.Priority priority;

	@NotBlank(message = "Адрес обязателен")
	private String address;

	@NotBlank(message = "Имя заявителя обязательно")
	private String applicantName;

	@PastOrPresent(message = "Время подачи не может быть в будущем")
	private LocalDateTime submissionTime = LocalDateTime.now();

	private String description;

	public RequestNewForm() {
	}

	/// Конвертация формы в заявку
	public Request toNewRequest() {
		return new Request(accidentType, priority, address, applicantName, submissionTime, description);
	}
}
