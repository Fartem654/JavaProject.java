package ru.fadeer.java_project_app.model.Converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.fadeer.java_project_app.model.Request;

/// Конвертер для статусов заявок. Нужен для корректного обмена данными между сущностями и БД
@Converter
public class RequestStatusConverter implements AttributeConverter<Request.Status, String> {

	/// Конвертация к БД
	@Override
	public String convertToDatabaseColumn(Request.Status status) {
		if (status == null) {
			return null;
		}
		return switch (status) {
			case Not_Accepted -> "Не принято";
			case Accepted -> "Принято";
			case In_Progress -> "В работе";
			case Completed -> "Завершено";
		};
	}

	/// Конвертация к сущности
	@Override
	public Request.Status convertToEntityAttribute(String status) {
		if (status == null) {
			return null;
		}
		return switch (status) {
			case "Не принято" -> Request.Status.Not_Accepted;
			case "Принято" -> Request.Status.Accepted;
			case "В работе" -> Request.Status.In_Progress;
			case "Завершено" -> Request.Status.Completed;
			default -> throw new IllegalArgumentException("Недопустимый статус: " + status);
		};
	}
}
