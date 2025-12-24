package ru.fadeer.java_project_app.dto.Assignments;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Assignment;

/// Класс-форма для промежуточного хранения назначения
@Getter
@Setter
public class AssignmentUpdateForm {
	@NotNull(message = "Время назначения обязательно")
	private LocalDateTime assignmentTime;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	public AssignmentUpdateForm() {
	}

	public AssignmentUpdateForm(Assignment assignment) {
		this.assignmentTime = assignment.getAssignmentTime();
		this.startTime = assignment.getStartTime();
		this.endTime = assignment.getEndTime();
	}

	/// Метод установки временных значений для полей: время назначения, начало работы, окончание работы
	/// @param assignment назначение в котором нужно установить поля
	/// @param form форма из которой взять новые значения
	public static void applyToAssignment(Assignment assignment, AssignmentUpdateForm form) {
		assignment.setAssignmentTime(form.getAssignmentTime());
		assignment.setStartTime(form.getStartTime());
		assignment.setEndTime(form.getEndTime());
	}
}
