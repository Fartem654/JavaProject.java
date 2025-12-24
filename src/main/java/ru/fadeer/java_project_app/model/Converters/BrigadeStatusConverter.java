package ru.fadeer.java_project_app.model.Converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.fadeer.java_project_app.model.Brigade;

/// Конвертер для статусов бригад. Нужен для корректного обмена данными между сущностями и БД
@Converter
public class BrigadeStatusConverter implements AttributeConverter<Brigade.Status, String> {

	/// Конвертация к БД
	@Override
	public String convertToDatabaseColumn(Brigade.Status status) {
		if (status == null) {
			return null;
		}
		return switch (status) {
			case free -> "Свободен";
			case inPlace -> "На месте";
			case inRoad -> "В пути";
			case disbanded -> "Расформирована";
		};
	}

	/// Конвертация к сущности
	@Override
	public Brigade.Status convertToEntityAttribute(String status) {
		if (status == null) {
			return null;
		}
		return switch (status) {
			case "Свободен" -> Brigade.Status.free;
			case "На месте" -> Brigade.Status.inPlace;
			case "В пути" -> Brigade.Status.inRoad;
			case "Расформирована" -> Brigade.Status.disbanded;
			default -> throw new IllegalArgumentException("Недопустимый статус: " + status);
		};
	}
}
