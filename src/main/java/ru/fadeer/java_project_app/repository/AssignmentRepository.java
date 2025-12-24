package ru.fadeer.java_project_app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fadeer.java_project_app.model.Assignment;

/// Интерфейс объединяющий сущности и БД. Наследует JpaRepository, генерирующий SQL запросы
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

	/// Метод для получения из БД списка связанных назначений с заявкой.
	/// @param id номер заявки в БД
	List<Assignment> findByRequestRequestId(Integer id);

}
