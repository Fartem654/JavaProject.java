package ru.fadeer.java_project_app.Controllers;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hibernate.TypeMismatchException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.fadeer.java_project_app.dto.Request.RequestEditForm;
import ru.fadeer.java_project_app.dto.Request.RequestNewForm;
import ru.fadeer.java_project_app.model.Request;
import ru.fadeer.java_project_app.service.RequestService;

/// Класс-контроллер, с методами для управления шаблонами.
@Controller
@RequestMapping("/requests")
public class RequestController {

	private final RequestService requestService;

	public RequestController(RequestService requestService) {
		this.requestService = requestService;
	}

	/// Метод для отображения шаблона с созданием новой заявки
	@GetMapping("/new")
	public String createForm(Model model) {
		RequestNewForm form = new RequestNewForm();
		form.setSubmissionTime(LocalDateTime.now());
		model.addAttribute("requestNewForm", form);
		model.addAttribute("priorities", Request.Priority.values());
		return "request/new";
	}

	/// Метод для обработки данных и их сохранения или возвращения на страницу с новой заявкой в случе ошибок
	@PostMapping
	public String createRequest(@Valid @ModelAttribute("requestNewForm") RequestNewForm requestNewForm,
			BindingResult bindingResult,
			Model model,
			@RequestParam Map<String, String> allParams) {

		model.addAttribute("priorities", Request.Priority.values());

		if (bindingResult.hasErrors()) {
			model.addAllAttributes(allParams);

			for (FieldError error : bindingResult.getFieldErrors()) {
				if (error.getField().equals("submissionTime") && error.contains(TypeMismatchException.class)) {
					model.addAttribute("submissionTimeError",
							"Некорректный формат даты. Используйте формат: ГГГГ-ММ-ДД ЧЧ:ММ (например: 2024-03-21 14:30)");
				}
			}

			return "request/new";
		}

		try {
			Request request = requestNewForm.toNewRequest();
			requestService.create(request);
			return "redirect:/requests";
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			model.addAllAttributes(allParams); // Сохраняем введенные данные
			return "request/new";
		} catch (Exception e) {
			// Обработка других исключений (например, конвертации даты)
			model.addAttribute("serverError",
					"Ошибка обработки данных: " + e.getMessage() +
							". Проверьте корректность введенных данных.");
			model.addAllAttributes(allParams);
			return "request/new";
		}
	}

	/// Метод для отображения шаблона изменения заявки
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, Model model) {
		Request request = requestService.findById(id);

		if (Request.Status.Completed.equals(request.getStatus())) {
			model.addAttribute("errorMessage", "Редактирование завершённых заявок запрещено.");
			return "request/edit";
		}

		RequestEditForm form = new RequestEditForm(request);
		model.addAttribute("editForm", form);
		model.addAttribute("priorities", Request.Priority.values());
		return "request/edit";
	}

	/// Обработка и занесение данных при изменении
	@PostMapping("/{id}")
	public String updateRequest(@PathVariable Integer id,
			@Valid @ModelAttribute("editForm") RequestEditForm form,
			BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			Request request = requestService.findById(id);
			form.setCurrentStatus(request.getStatus());
			model.addAttribute("editForm", form);
			model.addAttribute("priorities", Request.Priority.values());
			return "request/edit";
		}
		try {
			Request updated = RequestEditForm.applyRequest(form);
			requestService.update(id, updated);
			return "redirect:/requests";
		} catch (IllegalArgumentException e) {
			Request request = requestService.findById(id);
			form.setCurrentStatus(request.getStatus());
			model.addAttribute("editForm", form);
			bindingResult.reject("error.update", e.getMessage());
			model.addAttribute("priorities", Request.Priority.values());
			return "request/edit";
		}
	}

	/// Метод для отображения отдельной заявки
	@GetMapping("/{id}")
	public String viewRequest(@PathVariable Integer id, Model model) {
		Request request = requestService.findById(id);
		model.addAttribute("request", request);
		return "request/view";
	}
	/// Метод для отображения шаблона со всеми заявками
	@GetMapping
	public String listRequests(Model model) {
		List<Request> requests = requestService.findAll();
		model.addAttribute("requests", requests);
		return "request/list";
	}

	/// Метод для запроса на удаления заявки
	@PostMapping("/{id}/delete")
	public String deleteRequest(@PathVariable Integer id) {
		requestService.deleteById(id);
		return "redirect:/requests";
	}

	/// Метод для вывода шаблона с фильтрацией по статусу
	@GetMapping(params = "status")
	public String listByStatus(@RequestParam Request.Status status, Model model) {
		List<Request> requests = requestService.findByStatus(status);
		model.addAttribute("requests", requests);
		model.addAttribute("currentStatus", status);
		return "request/list";
	}
}