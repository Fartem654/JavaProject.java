package ru.fadeer.java_project_app.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Converters.RequestPriorityConverter;
import ru.fadeer.java_project_app.model.Converters.RequestStatusConverter;

/// Сущность заявка. Объект представляет строку из соответствующей таблицы "requests". Содержит
/// перечисления для корректной работы с БД
@Entity
@Table(name = "requests")
@Setter
@Getter
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer requestId;

	@Column(name = "accident_type", nullable = false)
	private String accidentType;

	@Column(name = "priority", nullable = false)
	@Convert(converter = RequestPriorityConverter.class)
	private Priority priority;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "applicant_name", nullable = false)
	private String applicantName;

	@Column(name = "submission_time", nullable = false)
	private LocalDateTime submissionTime;

	@Column(name = "description")
	private String description;

	@Convert(converter = RequestStatusConverter.class)
	@Column(name = "status", nullable = false)
	private Status status = Status.Not_Accepted;

	/// Хранит связанные с конкретной заявкой назначения. Каскадное удаление, авто удаление дочерних
	/// сущностей
	@OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Assignment> assignments = new ArrayList<>();

	/// Перечисление фиксированных значений БД для приоритетов
	public enum Priority {
		High("1", 1),
		Medium("2", 2),
		Low("3", 3);

		private final String toDisplayStr;
		private final int toDisplayInt;

		Priority(String toDisplayStr, int toDisplayInt) {
			this.toDisplayStr = toDisplayStr;
			this.toDisplayInt = toDisplayInt;
		}

		/// Метод вывода значений приоритета заявки в строчном виде
		public String getToDisplayStr() {
			return toDisplayStr;
		}

		/// Метод вывода значений приоритета заявки в нормальном виде
		public int getToDisplayInt() {
			return toDisplayInt;
		}
	}

	/// Перечисление фиксированных значений БД для статусов
	public enum Status {
		Not_Accepted("Не принято"),
		Accepted("Принято"),
		In_Progress("В работе"),
		Completed("Завершено");

		private final String toDisplay;

		Status(String toDisplay) {
			this.toDisplay = toDisplay;
		}

		/// Метод вывода значений статуса бригады в строчном виде
		public String getToDisplay() {
			return toDisplay;
		}
	}

	public Request() {
	}

	public Request(String accidentType, Priority priority, String address, String applicantName,
		LocalDateTime submissionTime, String description) {
		this.accidentType = accidentType;
		this.priority = priority;
		this.address = address;
		this.applicantName = applicantName;
		this.submissionTime = submissionTime;
		this.description = description;
	}

}
