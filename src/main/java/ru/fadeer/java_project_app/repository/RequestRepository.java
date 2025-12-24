package ru.fadeer.java_project_app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fadeer.java_project_app.model.Request;

/// Интерфейс объединяющий сущности и БД. Наследует JpaRepository, генерирующий SQL запросы
public interface RequestRepository extends JpaRepository<Request, Integer> {

	/// Метод для получения списка заявок с определенным статусом status
	/// @param status статус заявки
	List<Request> findByStatus(Request.Status status);

}