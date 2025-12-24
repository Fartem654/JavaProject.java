package ru.fadeer.java_project_app.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Request;

/// Класс-форма для промежуточного хранения заявки
@Getter
@Setter
public class RequestEditForm {
	private Integer requestId;

	@NotBlank(message = "Тип аварии обязателен")
	private String accidentType;

	// без @NotNull — может быть null, если недоступен
	private Request.Priority priority;

	@NotBlank(message = "Адрес обязателен")
	private String address;

	@NotBlank(message = "Имя заявителя обязательно")
	private String applicantName;

	private LocalDateTime submissionTime;

	private String description;

	private Request.Status currentStatus;

	public RequestEditForm() {}

	public RequestEditForm(Request request) {
		this.requestId = request.getRequestId();
		this.accidentType = request.getAccidentType();
		this.priority = request.getPriority();
		this.address = request.getAddress();
		this.applicantName = request.getApplicantName();
		this.submissionTime = request.getSubmissionTime();
		this.description = request.getDescription();
		this.currentStatus = request.getStatus();
	}

	/// Метод конвертации формы к заявке
	public static Request applyRequest(RequestEditForm form) {
		Request request = new Request();
		request.setRequestId(form.getRequestId());
		request.setAccidentType(form.getAccidentType());
		request.setPriority(form.getPriority());
		request.setAddress(form.getAddress());
		request.setApplicantName(form.getApplicantName());
		request.setSubmissionTime(form.getSubmissionTime());
		request.setDescription(form.getDescription());
		request.setStatus(null);
		return request;
	}
}

