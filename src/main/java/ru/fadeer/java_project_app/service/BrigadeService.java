package ru.fadeer.java_project_app.service;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fadeer.java_project_app.model.Brigade;
import ru.fadeer.java_project_app.model.Brigade.Status;
import ru.fadeer.java_project_app.repository.BrigadeRepository;

/// Класс-сервис с методами, описывающие основную логику работы приложения. Методы представляют
/// алгоритмы для создания, чтения, удаления, редактирования и др.
@Service
public class BrigadeService {

	private final BrigadeRepository brigadeRepository;

	public BrigadeService(BrigadeRepository brigadeRepository) {
		this.brigadeRepository = brigadeRepository;
	}

	/// Метод для создания новой бригады и записи ее в БД. Выполняется проверка данных и запись в БД
	/// со статусом "Свободен"
	@Transactional
	public Brigade create(Brigade brigade) {

		if (brigade.getBrigadeCode() == null || brigade.getBrigadeCode().isBlank()) {
			throw new IllegalArgumentException("Номер бригады обязателен.");

		}
		if (brigade.getBrigadeCode().matches(".*_v\\d+$")) {
			throw new IllegalArgumentException(
					"Код бригады не может заканчиваться на '_v...'. Это зарезервировано для архивных версий.");
		}
		if (brigadeRepository.existsByBrigadeCode(brigade.getBrigadeCode())) {
			throw new IllegalArgumentException(
					"Бригада с кодом '" + brigade.getBrigadeCode() + "' уже существует.");
		}

		if (brigade.getVehicleNumber() == null || brigade.getVehicleNumber().isBlank()) {
			throw new IllegalArgumentException("Номер машины обязателен");
		}

		if (brigade.getVehicleNumber().matches(".*_v\\d+$")) {
			throw new IllegalArgumentException(
					"Номер машины не может заканчиваться на '_v...'. Это зарезервировано для архивных версий.");
		}

		if (brigadeRepository.existsByVehicleNumber(brigade.getVehicleNumber())) {
			throw new IllegalArgumentException(
					"Машина с номером '" + brigade.getVehicleNumber() + "' уже назначена.");
		}

		if (brigade.getForemanName() == null || brigade.getForemanName().isBlank()) {
			throw new IllegalArgumentException("Имя бригадира обязательно.");
		}
		if (brigade.getForemanPhone() == null || brigade.getForemanPhone().isBlank()) {
			throw new IllegalArgumentException("Телефон бригадира обязателен");
		}

		brigade.setStatus(Status.free);

		brigade.setBrigadeId(null);

		return brigadeRepository.save(brigade);
	}

	/// Вывод всех бригад из бд
	@Transactional(readOnly = true)
	public List<Brigade> findAll() {
		return brigadeRepository.findAll();
	}

	/// Поиск бригады в Бд по id
	@Transactional(readOnly = true)
	public Brigade findById(Integer id) {
		return brigadeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Бригада с id: " + id + " не найдена."));
	}

	///  Данный метод позволяет устанавливать новые значения если они не нулевые, иначе ничего не
	/// происходит и значения в исходном параметре остаются такими же
	private <T> void copyNotNull(Consumer<T> action, T value) {
		if (value != null) {
			action.accept(value);
		}
	}

	/// Метод прямого обновления бригады Выполняет проверки и сохраняет новые данные
	///
	/// @param id             номер бригады для обновления
	/// @param updatedBrigade объект хранящий новые значения
	@Transactional
	public Brigade update(Integer id, Brigade updatedBrigade) {
		Brigade currentBrigade = findById(id);

		if (currentBrigade.getStatus() == Status.disbanded) {
			throw new IllegalArgumentException("Нельзя менять расформированную бригаду");
		}

		if (updatedBrigade.getBrigadeCode() != null &&
				!updatedBrigade.getBrigadeCode().equals(currentBrigade.getBrigadeCode())) {

			if (updatedBrigade.getBrigadeCode().matches(".*_v\\d+$")) {
				throw new IllegalArgumentException(
						"Код бригады не может заканчиваться на '_v...'. Это зарезервировано для архивных версий.");
			}

			if (brigadeRepository.existsByBrigadeCode(updatedBrigade.getBrigadeCode())) {
				throw new IllegalArgumentException(
						"Бригада с кодом '" + updatedBrigade.getBrigadeCode() + "' уже существует.");
			}
		}

		if (updatedBrigade.getVehicleNumber() != null &&
				!updatedBrigade.getVehicleNumber().equals(currentBrigade.getVehicleNumber())) {

			if (updatedBrigade.getVehicleNumber().matches(".*_v\\d+$")) {
				throw new IllegalArgumentException(
						"Номер машины не может заканчиваться на '_v...'. Это зарезервировано для архивных версий.");
			}

			if (brigadeRepository.existsByVehicleNumber(updatedBrigade.getVehicleNumber())) {
				throw new IllegalArgumentException(
						"Машина с номером '" + updatedBrigade.getVehicleNumber() + "' уже назначена.");
			}
		}

		copyNotNull(currentBrigade::setBrigadeCode, updatedBrigade.getBrigadeCode());
		copyNotNull(currentBrigade::setVehicleNumber, updatedBrigade.getVehicleNumber());
		copyNotNull(currentBrigade::setForemanName, updatedBrigade.getForemanName());
		copyNotNull(currentBrigade::setForemanPhone, updatedBrigade.getForemanPhone());

		return brigadeRepository.save(currentBrigade);
	}

//	/// Метод смены статуса на "Свободен"
//	@Transactional
//	void setStatusFree(Integer id) {
//		Brigade currentBrigade = findById(id);
//		if (currentBrigade.getStatus() == Brigade.Status.disbanded) {
//			throw new IllegalStateException("Нельзя изменить статус расформированной бригады");
//		}
//		currentBrigade.setStatus(Status.free);
//		brigadeRepository.save(currentBrigade);
//	}

	/// Метод смены статуса на "На месте"
	@Transactional
	void setStatusInPlace(Integer id) {
		Brigade currentBrigade = findById(id);
		if (currentBrigade.getStatus() == Brigade.Status.disbanded) {
			throw new IllegalStateException("Нельзя изменить статус расформированной бригады");
		}
		currentBrigade.setStatus(Status.inPlace);
		brigadeRepository.save(currentBrigade);
	}

//	/// Метод смены статуса на "В пути"
//	@Transactional
//	void setStatusInRoad(Integer id) {
//		Brigade currentBrigade = findById(id);
//		if (currentBrigade.getStatus() == Brigade.Status.disbanded) {
//			throw new IllegalStateException("Нельзя изменить статус расформированной бригады");
//		}
//		currentBrigade.setStatus(Status.inRoad);
//		brigadeRepository.save(currentBrigade);
//	}

	/// Генерация архивного номера бригады согласно ПО
	private String generateArchiveBrigadeCode(String brigadeCode) {
		String newBrigadeCode = brigadeCode;
		int version = 1;

		while (brigadeRepository.existsByBrigadeCode(newBrigadeCode + "_v" + version)) {
			version++;
		}
		return newBrigadeCode + "_v" + version;
	}

	/// Генерация архивного номера машины согласно ПО
	private String generateArchiveVehicleNumber(String vehicleNumber) {
		String newVehicleNumber = vehicleNumber;
		int version = 1;

		while (brigadeRepository.existsByVehicleNumber(newVehicleNumber + "_v" + version)) {
			version++;
		}
		return newVehicleNumber + "_v" + version;
	}

	/// Метод выполняющий обновление с архивацией согласно ПО. Сохраняется исходные имена для отката.
	/// Текущей бригаде задаются архивные имена. Проверяем уникальность новой бригады, откатываем если
	/// неуникальные
	///
	/// @param originalId номер архивируемой бригады
	/// @param updatedBrigade новые данные
	@Transactional
	public void updateAndArchive(Integer originalId, Brigade updatedBrigade) {
		Brigade currentBrigade = findById(originalId);

		if (currentBrigade.getStatus() == Status.disbanded) {
			throw new IllegalArgumentException("Нельзя заархивировать уже расформированную бригаду.");
		}

		if (currentBrigade.getStatus() != Status.free) {
			throw new IllegalArgumentException(
					"Нельзя заархивировать бригаду находящуюся на вызове. " +
							"Текущий статус: " + currentBrigade.getStatus().getToDisplay());
		}

		if (updatedBrigade.getBrigadeCode() != null &&
				updatedBrigade.getBrigadeCode().matches(".*_v\\d+$")) {
			throw new IllegalArgumentException(
					"Новый код бригады не может заканчиваться на '_v...'. Это зарезервировано для архивных версий.");
		}

		if (updatedBrigade.getVehicleNumber() != null &&
				updatedBrigade.getVehicleNumber().matches(".*_v\\d+$")) {
			throw new IllegalArgumentException(
					"Новый номер машины не может заканчиваться на '_v...'. Это зарезервировано для архивных версий.");
		}

		String originalBrigadeCode = currentBrigade.getBrigadeCode();
		String originalVehicleNumber = currentBrigade.getVehicleNumber();

		String archiveBrigadeCode = generateArchiveBrigadeCode(originalBrigadeCode);
		String archiveVehicleNumber = generateArchiveVehicleNumber(originalVehicleNumber);

		currentBrigade.setBrigadeCode(archiveBrigadeCode);
		currentBrigade.setVehicleNumber(archiveVehicleNumber);
		currentBrigade.setStatus(Status.disbanded);
		brigadeRepository.save(currentBrigade);

		if (brigadeRepository.existsByVehicleNumber(updatedBrigade.getVehicleNumber())) {
			currentBrigade.setBrigadeCode(originalBrigadeCode);
			currentBrigade.setVehicleNumber(originalVehicleNumber);
			currentBrigade.setStatus(Status.free);
			brigadeRepository.save(currentBrigade);

			throw new IllegalArgumentException(
					"Машина с номером '" + updatedBrigade.getVehicleNumber() + "' уже назначена.");
		}

		if (brigadeRepository.existsByBrigadeCode(updatedBrigade.getBrigadeCode())) {
			currentBrigade.setBrigadeCode(originalBrigadeCode);
			currentBrigade.setVehicleNumber(originalVehicleNumber);
			currentBrigade.setStatus(Status.free);
			brigadeRepository.save(currentBrigade);

			throw new IllegalArgumentException(
					"Бригада с кодом '" + updatedBrigade.getBrigadeCode() + "' уже существует.");
		}

		Brigade newBrigade = new Brigade();
		newBrigade.setBrigadeCode(updatedBrigade.getBrigadeCode());
		newBrigade.setVehicleNumber(updatedBrigade.getVehicleNumber());
		newBrigade.setForemanName(updatedBrigade.getForemanName());
		newBrigade.setForemanPhone(updatedBrigade.getForemanPhone());
		newBrigade.setStatus(Status.free);

		brigadeRepository.save(newBrigade);
	}

	/// Метод "удаления" бригады - установка статуса "Расформировано"
	@Transactional
	public void softDelete(Integer id) {
		Brigade brigade = brigadeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Бригада с id: " + id + "не найдена."));
		if (brigade.getStatus() != Status.free) {
			throw new IllegalArgumentException(
					"Нельзя расформировать бригаду, которая находится на задании. " +
							"Текущий статус: " + brigade.getStatus().getToDisplay());
		}
		if (brigade.getStatus() != Status.disbanded) {
			brigade.setStatus(Status.disbanded);
			brigadeRepository.save(brigade);
		}
	}

	/// Метод физического удаления бригады
	@Transactional
	public void hardDelete(Integer id) {
		brigadeRepository.deleteById(id);
	}

}
