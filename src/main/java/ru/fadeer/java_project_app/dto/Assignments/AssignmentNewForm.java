package ru.fadeer.java_project_app.dto.Assignments;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Assignment;

/// Класс-форма для промежуточного хранения нового назначения
@Getter
@Setter
public class AssignmentNewForm {

	@NotNull(message = "ID бригады обязателен")
	private Integer brigadeId;

	@NotNull(message = "ID заявки обязателен")
	private Integer requestId;

	@NotNull(message = "Время назначения обязательно")
	private LocalDateTime assignmentTime;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	public AssignmentNewForm() {
		this.assignmentTime = LocalDateTime.now();
	}

	/// Конвертация формы в назначение
	/// Бригады и заявки устанавливаются в сервисе
	public Assignment toNewAssignment() {
		Assignment assignment = new Assignment();
		assignment.setAssignmentTime(this.assignmentTime);
		assignment.setStartTime(this.startTime);
		assignment.setEndTime(this.endTime);
		return assignment;
	}
}
