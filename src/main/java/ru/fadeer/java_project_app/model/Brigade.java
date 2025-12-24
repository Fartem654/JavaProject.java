package ru.fadeer.java_project_app.model;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ru.fadeer.java_project_app.model.Converters.BrigadeStatusConverter;


/// Сущность Бригада. Объект представляет строку из соответствующей таблицы "brigades". Содержит
/// перечисления для корректной работы с БД
@Entity
@Table(name = "brigades")
@Setter
@Getter
public class Brigade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer brigadeId;

	@Column(name = "brigade_code", unique = true, nullable = false)
	private String brigadeCode;

	@Column(name = "vehicle_number", nullable = false)
	private String vehicleNumber;

	@Column(name = "foreman_name", nullable = false)
	private String foremanName;

	@Column(name = "foreman_phone", nullable = false)
	private String foremanPhone;

	@Convert(converter = BrigadeStatusConverter.class)
	@Column(name = "status", nullable = false)
	private Status status = Status.free;

	/// Хранит связанные с конкретной бригадой назначения. Каскадное удаление, авто удаление дочерних
	/// сущностей
	@OneToMany(mappedBy = "brigade", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Assignment> assignments = new ArrayList<>();

	/// Перечисление фиксированных значений БД для статусов
	public enum Status {
		inPlace("На месте"),
		inRoad("В пути"),
		free("Свободен"),
		disbanded("Расформирована");

		private final String toDisplay;

		Status(String display) {
			this.toDisplay = display;
		}

		/// Метод вывода значений статуса бригады в нормальном виде
		public String getToDisplay() {
			return toDisplay;
		}
	}

	public Brigade() {
	}

//	public Brigade(String brigadeCode, String vehicleNumber, String foremanName,
//			String foremanPhone) {
//		this.brigadeCode = brigadeCode;
//		this.vehicleNumber = vehicleNumber;
//		this.foremanName = foremanName;
//		this.foremanPhone = foremanPhone;
//	}
//
//	public Brigade(Brigade brigade) {
//		this.brigadeCode = brigade.getBrigadeCode();
//		this.vehicleNumber = brigade.getVehicleNumber();
//		this.foremanName = brigade.getForemanName();
//		this.foremanPhone = brigade.getForemanPhone();
//	}
}
