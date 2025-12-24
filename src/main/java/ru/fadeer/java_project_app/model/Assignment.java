package ru.fadeer.java_project_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/// Сущность Назначение. Объект представляет строку из соответствующей таблицы "assignments"
@Entity
@Table(name = "assignments")
@Setter
@Getter
public class Assignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer assignmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "brigade_id", nullable = false)
	private Brigade brigade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "request_id", nullable = false)
	private Request request;

	@Column(name = "assignment_time", nullable = false)
	private LocalDateTime assignmentTime;

	@Column(name = "start_time")
	private LocalDateTime startTime;

	@Column(name = "end_time")
	private LocalDateTime endTime;

}
