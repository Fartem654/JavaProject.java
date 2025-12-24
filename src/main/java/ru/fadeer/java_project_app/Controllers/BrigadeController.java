package ru.fadeer.java_project_app.Controllers;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.fadeer.java_project_app.dto.Brigade.BrigadeNewForm;
import ru.fadeer.java_project_app.dto.Brigade.BrigadeUpdateForm;
import ru.fadeer.java_project_app.model.Brigade;
import ru.fadeer.java_project_app.model.Brigade.Status;
import ru.fadeer.java_project_app.service.BrigadeService;

/// Класс-контроллер, с методами для управления шаблонами.
@Controller
@RequestMapping("/brigades")
public class BrigadeController {

	private final BrigadeService brigadeService;

	public BrigadeController(BrigadeService brigadeService) {
		this.brigadeService = brigadeService;
	}

	/// Метод для отображения шаблона с созданием новой бригадой
	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("brigadeForm", new BrigadeNewForm());
		return "brigade/new";
	}

	/// Метод для обработки данных и их сохранения или возвращения на страницу с новой бригадой в случе ошибок
	@PostMapping
	public String createBrigade(@Valid @ModelAttribute("brigadeForm") BrigadeNewForm form,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return "brigade/new";
		}

		try {
			brigadeService.create(form.toNewBrigade());
			return "redirect:/brigades";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "brigade/new";
		}
	}

	/// Метод для отображения шаблона со всеми бригадами
	@GetMapping
	public String listBrigades(Model model) {
		List<Brigade> brigades = brigadeService.findAll();
		model.addAttribute("brigades", brigades);
		return "brigade/list";
	}

	/// Метод для отображения отдельной бригады
	@GetMapping("/{id}")
	public String viewBrigade(@PathVariable Integer id, Model model) {
		Brigade brigade = brigadeService.findById(id);
		model.addAttribute("brigade", brigade);
		return "brigade/view";
	}

	/// Метод для отображения шаблона изменения бригады
	///
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, Model model) {
		try {
			Brigade brigade = brigadeService.findById(id);

			if (brigade.getStatus() == Status.disbanded) {
				model.addAttribute("error", "Нельзя редактировать расформированную бригаду");
				return "redirect:/brigades" + id;
			}

			BrigadeUpdateForm form = new BrigadeUpdateForm(brigade);
			model.addAttribute("brigadeForm", form);
			model.addAttribute("brigade", brigade); // Добавляем бригаду в модель
			return "brigade/edit";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return "redirect:/brigades";
		}
	}

	/// Обработка и занесение данных при изменении, учитывая два способа
	@PostMapping("/{id}")
	public String updateBrigade(@PathVariable Integer id,
			@Valid @ModelAttribute("brigadeForm") BrigadeUpdateForm form, BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			try {
				Brigade brigade = brigadeService.findById(id);
				model.addAttribute("brigade", brigade);
			} catch (IllegalArgumentException e) {
				model.addAttribute("error", e.getMessage());
				return "redirect:/brigades";
			}
			return "brigade/edit";
		}

		try {
			if (Boolean.TRUE.equals(form.getArchive())) {
				brigadeService.updateAndArchive(id, form.toBrigade());
				return "redirect:/brigades";
			} else {
				brigadeService.update(id, form.toBrigade());
				return "redirect:/brigades/" + id;
			}
		} catch (IllegalArgumentException e) {
			try {
				Brigade brigade = brigadeService.findById(id);
				model.addAttribute("brigade", brigade);
			} catch (IllegalArgumentException ex) {
				model.addAttribute("error", ex.getMessage());
				return "redirect:/brigades";
			}
			model.addAttribute("error", e.getMessage());
			return "brigade/edit";
		}
	}

	/// Метод для "удаления" бригады
	@PostMapping("/{id}/soft-delete")
	public String softDeleteBrigade(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		try {
			brigadeService.softDelete(id);
			redirectAttributes.addFlashAttribute("success", "Бригада успешно расформирована");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/brigades/" + id;
	}

	/// Метод для запроса на физическое удаление
	@PostMapping("/{id}/hard-delete")
	public String hardDeleteBrigade(@PathVariable Integer id) {
		brigadeService.hardDelete(id);
		return "redirect:/brigades";
	}

}
