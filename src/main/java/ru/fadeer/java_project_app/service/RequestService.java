package ru.fadeer.java_project_app.service;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fadeer.java_project_app.model.Assignment;
import ru.fadeer.java_project_app.model.Brigade;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.PersistenceContext;
import ru.fadeer.java_project_app.model.Request;
import ru.fadeer.java_project_app.model.Request.Status;
import ru.fadeer.java_project_app.repository.BrigadeRepository;
import ru.fadeer.java_project_app.repository.RequestRepository;

/// Класс-сервис с методами, описывающие основную логику работы приложения. Методы представляют
/// алгоритмы для создания, чтения, удаления, редактирования и др.
@Service
public class RequestService {

	private final RequestRepository requestRepository;
	private final BrigadeRepository brigadeRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public RequestService(RequestRepository requestRepository,
			BrigadeRepository brigadeRepository) {
		this.requestRepository = requestRepository;
		this.brigadeRepository = brigadeRepository;
	}

	/// Метод для создания новой заявки и записи ее в БД. Выполняется проверка данных и запись в БД со
	/// статусом "Не принято"
	@Transactional
	public Request create(Request request) {
		if (request.getAccidentType() == null || request.getAccidentType().isBlank()) {
			throw new IllegalArgumentException("Тип аварии обязателен.");
		}
		if (request.getPriority() == null) {
			throw new IllegalArgumentException("Приоритет обязателен.");
		}

		if (request.getAddress() == null || request.getAddress().isBlank()) {
			throw new IllegalArgumentException("Адрес обязателен.");
		}

		if (request.getApplicantName() == null || request.getApplicantName().isBlank()) {
			throw new IllegalArgumentException("Имя заявителя обязательно.");
		}

		if (request.getSubmissionTime() == null) {
			request.setSubmissionTime(LocalDateTime.now());
		}

		if (LocalDateTime.now().isBefore(request.getSubmissionTime())) {
			throw new IllegalArgumentException("Время назначения не может быть позже текущего момента.");
		}

		request.setRequestId(null);

		return requestRepository.save(request);
	}

	/// Метод поиска одной заявки
	@Transactional(readOnly = true)
	public Request findById(Integer id) {
		return requestRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Заявка с id: " + id + "не найдена."));
	}

	/// Метод для вывода всех заявок
	@Transactional(readOnly = true)
	public List<Request> findAll() {
		return requestRepository.findAll();
	}

	/// Метод для вывода всех заявок с определенным статусом
	public List<Request> findByStatus(Request.Status status) {
		if (status == null) {
			return findAll();
		}
		return requestRepository.findByStatus(status);
	}


	///  Данный метод позволяет устанавливать новые значения если они не нулевые, иначе ничего не
	/// происходит и значения в исходном параметре остаются такими же
	private <T> void copyNotNull(Consumer<T> action, T value) {
		if (value != null) {
			action.accept(value);
		}
	}

	/// Метод для обновления заявки. Выполняется проверка, далее исходя из статуса записываются
	/// значения, соблюдая правила ПО
	///
	/// @param id             номер обновляемой заявки
	/// @param updatedRequest новые данные
	@Transactional
	public Request update(Integer id, Request updatedRequest) {
		Request currentRequest = findById(id);

		if (updatedRequest.getRequestId() != null && !updatedRequest.getRequestId().equals(id)) {
			throw new IllegalArgumentException("Нельзя изменить номер(id) заявки!");
		}

		if (updatedRequest.getStatus() != null) {
			throw new IllegalArgumentException(
					"Статус заявки нельзя изменять напрямую. Используйте связные назначения бригады и аттрибуты в нем.");
		}

		switch (currentRequest.getStatus()) {
			case Not_Accepted:
				copyNotNull(currentRequest::setAccidentType, updatedRequest.getAccidentType());
				copyNotNull(currentRequest::setAddress, updatedRequest.getAddress());
				copyNotNull(currentRequest::setPriority, updatedRequest.getPriority());
				copyNotNull(currentRequest::setApplicantName, updatedRequest.getApplicantName());
				copyNotNull(currentRequest::setDescription, updatedRequest.getDescription());
				copyNotNull(currentRequest::setSubmissionTime, updatedRequest.getSubmissionTime());
				break;
			case Accepted:
				copyNotNull(currentRequest::setAccidentType, updatedRequest.getAccidentType());
				copyNotNull(currentRequest::setAddress, updatedRequest.getAddress());
				copyNotNull(currentRequest::setApplicantName, updatedRequest.getApplicantName());
				copyNotNull(currentRequest::setDescription, updatedRequest.getDescription());
				copyNotNull(currentRequest::setSubmissionTime, updatedRequest.getSubmissionTime());
				break;
			case In_Progress:
				copyNotNull(currentRequest::setAccidentType, updatedRequest.getAccidentType());
				copyNotNull(currentRequest::setApplicantName, updatedRequest.getApplicantName());
				copyNotNull(currentRequest::setDescription, updatedRequest.getDescription());
				copyNotNull(currentRequest::setSubmissionTime, updatedRequest.getSubmissionTime());
				break;
			case Completed:
				throw new IllegalArgumentException("Нельзя менять завершенную заявку.");
		}
		return requestRepository.save(currentRequest);
	}

	/// Вызывается если с заявки снимаются все назначения
	@Transactional
	void setStatusNotAccepted(Integer id) {
		Request request = findById(id);
		request.setStatus(Status.Not_Accepted);
	}

	/// Вызывается если в связанном назначении поля начало и окончание работ null
	@Transactional
	void setStatusAccepted(Integer id) {
		Request request = findById(id);
		request.setStatus(Request.Status.Accepted);
	}

	/// Вызывается если в связанном назначении поле начало работ не null, а поле окончание работ null
	@Transactional
	void setStatusInProgress(Integer id) {
		Request request = findById(id);
		request.setStatus(Status.In_Progress);
	}

	/// Вызывается если в связанном назначении поле начало работ и окончание работ не null
	@Transactional
	void setStatusCompleted(Integer id) {
		Request request = findById(id);
		request.setStatus(Status.Completed);
	}

	/// Метод удаления заявки. Обновление статуса бригад вызывается в других вызовах.
	@Transactional
	public void deleteById(Integer id) {
		// Просто удаляем все через native queries - никаких проблем с контекстом

		// 1. Удаляем назначения
		entityManager.createNativeQuery(
						"DELETE FROM assignments WHERE request_id = :id")
				.setParameter("id", id)
				.executeUpdate();

		int deleted = entityManager.createNativeQuery(
						"DELETE FROM requests WHERE request_id = :id")
				.setParameter("id", id)
				.executeUpdate();

		if (deleted == 0) {
			throw new IllegalArgumentException("Заявка не найдена");
		}

		entityManager.createNativeQuery(
						"UPDATE brigades b SET status = 'Свободен' " +
								"WHERE status != 'Расформирована' AND " +
								"NOT EXISTS (SELECT 1 FROM assignments a WHERE a.brigade_id = b.brigade_id AND a.end_time IS NULL)")
				.executeUpdate();
	}

}
