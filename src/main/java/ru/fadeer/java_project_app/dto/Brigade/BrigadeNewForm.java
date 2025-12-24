package ru.fadeer.java_project_app.dto.Brigade;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Brigade;

/// Класс-форма для промежуточного хранения новой бригады
@Getter
@Setter
public class BrigadeNewForm {

	@NotBlank(message = "Код бригады обязателен")
	private String brigadeCode;

	@NotBlank(message = "Номер машины обязателен")
	private String vehicleNumber;

	@NotBlank(message = "Имя бригадира обязательно")
	private String foremanName;

	@NotBlank(message = "Телефон бригадира обязателен")
	private String foremanPhone;

	public BrigadeNewForm() {

	}

	/// Конвертация формы в бригаду
	public Brigade toNewBrigade() {
		Brigade brigade = new Brigade();
		brigade.setBrigadeCode(this.brigadeCode);
		brigade.setVehicleNumber(this.vehicleNumber);
		brigade.setForemanName(this.foremanName);
		brigade.setForemanPhone(this.foremanPhone);
		return brigade;
	}

}
