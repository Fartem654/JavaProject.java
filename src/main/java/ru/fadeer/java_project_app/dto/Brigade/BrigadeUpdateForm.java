package ru.fadeer.java_project_app.dto.Brigade;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Brigade;

/// Класс-форма для промежуточного хранения бригады
@Getter
@Setter
public class BrigadeUpdateForm {
	@NotBlank(message = "Код бригады обязателен")
	private String brigadeCode;

	@NotBlank(message = "Номер машины обязателен")
	private String vehicleNumber;

	@NotBlank(message = "Имя бригадира обязательно")
	private String foremanName;

	@NotBlank(message = "Телефон бригадира обязателен")
	private String foremanPhone;

	/// Доп поле-индикатор архивирования данных
	private Boolean archive = false;

	public BrigadeUpdateForm() {
	}

	public BrigadeUpdateForm(Brigade brigade) {
		this.brigadeCode = brigade.getBrigadeCode();
		this.vehicleNumber = brigade.getVehicleNumber();
		this.foremanName = brigade.getForemanName();
		this.foremanPhone = brigade.getForemanPhone();
		this.archive = false;
	}

	/// Конвертация формы в бригаду
	public Brigade toBrigade() {
		Brigade brigade = new Brigade();
		brigade.setBrigadeCode(this.brigadeCode);
		brigade.setVehicleNumber(this.vehicleNumber);
		brigade.setForemanName(this.foremanName);
		brigade.setForemanPhone(this.foremanPhone);
		return brigade;
	}
}
