package ru.fadeer.java_project_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fadeer.java_project_app.dto.Assignments.AssignmentUpdateForm;
import ru.fadeer.java_project_app.model.Assignment;
import ru.fadeer.java_project_app.model.Brigade;
import ru.fadeer.java_project_app.model.Request;
import ru.fadeer.java_project_app.repository.AssignmentRepository;
import ru.fadeer.java_project_app.repository.BrigadeRepository;
import ru.fadeer.java_project_app.repository.RequestRepository;

/// Класс-сервис с методами, описывающие основную логику работы приложения. Методы представляют алгоритмы для
/// создания, чтения, удаления, редактирования и др.
@Service
public class AssignmentService {

	private final AssignmentRepository assignmentRepository;
	private final BrigadeRepository brigadeRepository;
	private final RequestRepository requestRepository;
	private final BrigadeService brigadeService;
	private final RequestService requestService;

	public AssignmentService(
			AssignmentRepository assignmentRepository,
			BrigadeService brigadeService,
			RequestService requestService,
			BrigadeRepository brigadeRepository,
			RequestRepository requestRepository) {
		this.assignmentRepository = assignmentRepository;
		this.brigadeService = brigadeService;
		this.requestService = requestService;
		this.brigadeRepository = brigadeRepository;
		this.requestRepository = requestRepository;
	}

	/// Метод нормализующий статусы заявок согласно правилам ПО. Проверяются все назначения заявки, и
	/// исходя из их количества или содержимого устанавливается статус
	///
	/// @param requestId номер заявки, у которой проверятся состояния назначений
	private void setNormalRequestStatus(Integer requestId) {
		Request request = requestService.findById(requestId);
		List<Assignment> assignments = assignmentRepository.findByRequestRequestId(requestId);

		if (assignments.isEmpty()) {
			request.setStatus(Request.Status.Not_Accepted);
		} else {
			boolean hasAnyEndTime = assignments.stream()
					.anyMatch(a -> a.getEndTime() != null);

			boolean hasAnyStartTimeWithoutEnd = assignments.stream()
					.anyMatch(a -> a.getStartTime() != null && a.getEndTime() == null);

			boolean allWithoutStartTime = assignments.stream()
					.allMatch(a -> a.getStartTime() == null);

			if (hasAnyEndTime) {
				request.setStatus(Request.Status.Completed);
				completeAllAssignmentsForRequest(requestId);
			} else if (hasAnyStartTimeWithoutEnd) {
				request.setStatus(Request.Status.In_Progress);
			} else if (allWithoutStartTime) {
				request.setStatus(Request.Status.Accepted);
			}
		}

		requestRepository.save(request);
	}

	/// Метод валидации времени проверяет поля времени назначения, старта и окончания работ
	/// назначения, далее выбрасывает ошибки, исходя из правил ПО
	///
	/// @param assignment назначение у которого необходимо проверить правильность времени
	private void checkTime(Assignment assignment) {
		LocalDateTime at = assignment.getAssignmentTime();
		LocalDateTime st = assignment.getStartTime();
		LocalDateTime et = assignment.getEndTime();

		if (at == null) {
			throw new IllegalArgumentException("Время назначения не может быть пустым.");
		}

		if (LocalDateTime.now().isBefore(at)) {
			throw new IllegalArgumentException("Время назначения не может быть позже текущего момента.");
		}

		if (st != null && st.isBefore(at)) {
			throw new IllegalArgumentException("Работа не может быть начата раньше назначения.");
		}

		if (et != null && st == null) {
			throw new IllegalArgumentException("Нельзя завершить работу не начав ее.");
		}

		if (et != null && et.isBefore(st)) {
			throw new IllegalArgumentException("Время окончания не может быть раньше начала.");
		}
	}

	/// Метод для установки текущего статуса бригады. Проверяются все назначения, далее если найдено
	/// активное назначение, меняется статус согласно ПО
	///
	/// @param brigadeId номер бригады, у которой необходимо обновить статус
	private void updateBrigadeStatus(Integer brigadeId) {
		Brigade brigade = brigadeRepository.findById(brigadeId)
				.orElseThrow(
						() -> new IllegalArgumentException("Бригада с id: " + brigadeId + " не найдена."));

		if (brigade.getStatus() == Brigade.Status.disbanded) {
			return;
		}

		boolean hasActiveAssignments = brigade.getAssignments().stream()
				.anyMatch(a -> a.getEndTime() == null);

		if (!hasActiveAssignments) {
			brigade.setStatus(Brigade.Status.free);
			brigadeRepository.save(brigade);
		} else {
			boolean isOnSite = brigade.getAssignments().stream()
					.anyMatch(a -> a.getStartTime() != null && a.getEndTime() == null);

			Brigade.Status newStatus = isOnSite ? Brigade.Status.inPlace : Brigade.Status.inRoad;

			if (brigade.getStatus() != newStatus) {
				brigade.setStatus(newStatus);
				brigadeRepository.save(brigade);
			}
		}
	}


	/// Метод выполняющий назначение бригад на заявки. Работает с БД через соответсвующий репозиторий.
	/// Выполнятся валидация, создается назначение и передается в списки связанных сущностей. Далее
	/// меняются статусы.
	///
	/// @param brigadeId номер назначенной бригады
	/// @param requestId номер выбранной заявки
	@Transactional
	public Assignment assignBrigade(Integer requestId, Integer brigadeId) {
		Brigade brigade = brigadeService.findById(brigadeId);
		Request request = requestService.findById(requestId);

		if (brigade.getStatus() == Brigade.Status.disbanded) {
			throw new IllegalArgumentException("Нельзя назначить расформированную бригаду.");
		}

		if (brigade.getStatus() != Brigade.Status.free) {
			throw new IllegalArgumentException("Назначить можно только свободную бригаду.");
		}

		if (request.getStatus() == Request.Status.Completed) {
			throw new IllegalArgumentException(
					"Бригаду можно назначить только не на завершенную заявку.");
		}

		Assignment assignment = new Assignment();
		assignment.setRequest(request);
		assignment.setBrigade(brigade);
		assignment.setAssignmentTime(LocalDateTime.now());

		request.getAssignments().add(assignment);
		brigade.getAssignments().add(assignment);

		Assignment saved = assignmentRepository.save(assignment);

		setNormalRequestStatus(requestId);

		brigade.setStatus(Brigade.Status.inRoad);
		brigadeRepository.save(brigade);

		return saved;
	}

	/// Поиск назначения в Бд по id
	@Transactional(readOnly = true)
	public Assignment findById(Integer id) {
		return assignmentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Назначение с id: " + id + " не найдено."));
	}

	/// Вывод всех назначений из БД
	@Transactional(readOnly = true)
	public List<Assignment> findAll() {
		return assignmentRepository.findAll();
	}

	/// Метод отвечающий за установку текущего времени на начало работ и меняющий связанные статусы
	@Transactional
	public void startWork(Integer assignmentId) {
		Assignment assignment = findById(assignmentId);
		if (assignment.getStartTime() != null) {
			throw new IllegalArgumentException("Работа уже начата в " + assignment.getStartTime());
		}
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(assignment.getAssignmentTime())) {
			throw new IllegalArgumentException("Работа не может быть начата раньше назначения.");
		}

		assignment.setStartTime(now);
		assignmentRepository.save(assignment);

		setNormalRequestStatus(assignment.getRequest().getRequestId());
		brigadeService.setStatusInPlace(assignment.getBrigade().getBrigadeId());
	}

	/// Метод отвечающий за установку текущего времени на окончания работ и меняющий связанные статусы
	/// согласно ПО
	@Transactional
	public void completeWork(Integer assignmentId) {
		Assignment assignment = findById(assignmentId);

		if (assignment.getStartTime() == null) {
			throw new IllegalArgumentException("Нельзя завершить работу не начав ее.");
		}

		if (assignment.getEndTime() != null) {
			throw new IllegalArgumentException("Работа уже завершена в " + assignment.getEndTime());
		}

		LocalDateTime now = LocalDateTime.now();
		if (assignment.getStartTime().isAfter(now)) {
			now = assignment.getStartTime();
		}
		if (now.isBefore(assignment.getStartTime())) {
			throw new IllegalArgumentException("Время окончания не может быть раньше начала.");
		}

		List<Assignment> assignments = assignmentRepository.findByRequestRequestId(
				assignment.getRequest().getRequestId());
		System.err.println(assignments.size());
		for (Assignment a : assignments) {
			if (a.getEndTime() == null) {
				if (a.getStartTime() == null) {
					a.setStartTime(now);
				}
				a.setEndTime(now);
				assignmentRepository.save(a);

				Brigade brigade = a.getBrigade();
				if (brigade.getStatus() != Brigade.Status.disbanded) {
					brigade.setStatus(Brigade.Status.free);
					brigadeRepository.save(brigade);
				}
			}
		}

		assignment.setEndTime(now);
		assignmentRepository.save(assignment);

		setNormalRequestStatus(assignment.getRequest().getRequestId());
	}

	///  Данный метод позволяет устанавливать новые значения если они не нулевые, иначе ничего не
	/// происходит и значения в исходном параметре остаются такими же
	private <T> void copyNotNull(Consumer<T> action, T value) {
		if (value != null) {
			action.accept(value);
		}
	}

	/// Метод для установки значения в поле окончания работ всех назначений одной заявки, а также
	/// обновление статуса соответствующей бригады
	///
	/// @param requestId номер заявки, у которой нужно завершить все назначения
	/// @param endTime   время окончания работ над заявкой
	@Transactional
	public void completeAllAssignmentsForRequest(Integer requestId, LocalDateTime endTime) {
		List<Assignment> assignments = assignmentRepository.findByRequestRequestId(requestId);

		for (Assignment assignment : assignments) {
			if (assignment.getEndTime() == null) {
				// Если работа не начата, ставим время начала = время окончания
				if (assignment.getStartTime() == null) {
					assignment.setStartTime(endTime);
				}
				// Ставим время окончания
				assignment.setEndTime(endTime);
				assignmentRepository.save(assignment);

				// Обновляем статус бригады
				Brigade brigade = assignment.getBrigade();
				if (brigade.getStatus() != Brigade.Status.disbanded) {
					brigade.setStatus(Brigade.Status.free);
					brigadeRepository.save(brigade);
				}
			}
		}
	}

	/// Перегрузка метода completeAllAssignmentsForRequest для установки времени на данный момент
	@Transactional
	public void completeAllAssignmentsForRequest(Integer requestId) {
		completeAllAssignmentsForRequest(requestId, LocalDateTime.now());
	}

	/// Метод обновления назначения, с последующим обновлением статусов
	///
	/// @param assignmentId номер назначения для обновления
	/// @param dto Форма с новыми данными
	@Transactional
	public void update(Integer assignmentId, AssignmentUpdateForm dto) {
		Assignment assignment = findById(assignmentId);

		assignment.setAssignmentTime(dto.getAssignmentTime());
		assignment.setStartTime(dto.getStartTime());
		assignment.setEndTime(dto.getEndTime());

		checkTime(assignment);

		Assignment saved = assignmentRepository.save(assignment);

		setNormalRequestStatus(saved.getRequest().getRequestId());
		updateBrigadeStatus(saved.getBrigade().getBrigadeId());
	}


	/// Метод для удаления назначения и последующим изменением статусов по правилам ПО
	///
	/// @param assignmentId номер назначения для удаления
	@Transactional
	public void delete(Integer assignmentId) {
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> {
					return new IllegalArgumentException("Назначение не найдено");
				});

		Brigade brigade = assignment.getBrigade();

		if (brigade.getStatus() != Brigade.Status.disbanded) {
			brigade.setStatus(Brigade.Status.free);
			brigadeRepository.save(brigade);
		}

		if (assignment.getRequest().getStatus() != Request.Status.Completed) {
			assignmentRepository.deleteById(assignmentId);

			setNormalRequestStatus(assignment.getRequest().getRequestId());

		} else {
			assignmentRepository.deleteById(assignmentId);
		}


	}

}
