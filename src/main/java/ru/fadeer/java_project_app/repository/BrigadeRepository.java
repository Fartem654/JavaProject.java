package ru.fadeer.java_project_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fadeer.java_project_app.model.Brigade;

/// Интерфейс объединяющий сущности и БД. Наследует JpaRepository, генерирующий SQL запросы
public interface BrigadeRepository extends JpaRepository<Brigade, Integer> {

	/// Метод проверки существования мышины с номером vehicleNumber
	/// @param vehicleNumber номер машины
	boolean existsByVehicleNumber(String vehicleNumber);

	/// Метод проверки существования бригады с кодом brigadeCode
	/// @param brigadeCode номер бригады
	boolean existsByBrigadeCode(String brigadeCode);
}
