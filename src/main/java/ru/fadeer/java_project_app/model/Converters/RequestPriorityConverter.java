package ru.fadeer.java_project_app.model.Converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.fadeer.java_project_app.model.Request;

///Конвертер для приоритетов заявок. Нужен для корректного обмена данными между сущностями и БД
@Converter
public class RequestPriorityConverter implements AttributeConverter<Request.Priority, Integer> {

	/// Конвертация к БД
	@Override
	public Integer convertToDatabaseColumn(Request.Priority priority) {
		return switch (priority) {
			case High -> 1;
			case Medium -> 2;
			case Low -> 3;
		};
	}

	/// Конвертация к сущности
	@Override
	public Request.Priority convertToEntityAttribute(Integer priority) {
		return switch (priority) {
			case 1 -> Request.Priority.High;
			case 2 -> Request.Priority.Medium;
			case 3 -> Request.Priority.Low;
			default -> throw new IllegalArgumentException("Недопустимый приоритет: " + priority);
		};
	}
}
