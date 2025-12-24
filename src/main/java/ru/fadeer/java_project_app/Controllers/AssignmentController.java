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
import org.springframework.web.bind.annotation.RequestParam;
import ru.fadeer.java_project_app.dto.Assignments.AssignmentNewForm;
import ru.fadeer.java_project_app.dto.Assignments.AssignmentUpdateForm;
import ru.fadeer.java_project_app.model.Assignment;
import ru.fadeer.java_project_app.model.Brigade;
import ru.fadeer.java_project_app.model.Request;
import ru.fadeer.java_project_app.service.AssignmentService;
import ru.fadeer.java_project_app.service.BrigadeService;
import ru.fadeer.java_project_app.service.RequestService;

/// Класс-контроллер, с методами для управления шаблонами.
@Controller
@RequestMapping("/assignments")
public class AssignmentController {

	private final AssignmentService assignmentService;
	private final BrigadeService brigadeService;
	private final RequestService requestService;

	public AssignmentController(AssignmentService assignmentService, BrigadeService brigadeService,
			RequestService requestService) {
		this.assignmentService = assignmentService;
		this.brigadeService = brigadeService;
		this.requestService = requestService;
	}

	/// Метод для отображения шаблона с созданием нового назначения
	@GetMapping("/assign")
	public String assignForm(@RequestParam(required = false) Integer requestId, Model model) {
		AssignmentNewForm form = new AssignmentNewForm();

		if (requestId != null) {
			form.setRequestId(requestId);
		}

		List<Brigade> freeBrigades = brigadeService.findAll().stream()
				.filter(b -> b.getStatus() == Brigade.Status.free)
				.toList();

		List<Request> activeRequests = requestService.findAll().stream()
				.filter(r -> r.getStatus() != Request.Status.Completed)
				.toList();

		model.addAttribute("assignmentForm", form);
		model.addAttribute("freeBrigades", freeBrigades);
		model.addAttribute("activeRequests", activeRequests);
		return "assignment/new";
	}

	/// Метод для обработки данных и их сохранения или возвращения на страницу с новым назначением в случе ошибок
	@PostMapping
	public String createAssignment(@Valid @ModelAttribute("assignmentForm") AssignmentNewForm form,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {

			List<Brigade> freeBrigades = brigadeService.findAll().stream()
					.filter(b -> b.getStatus() == Brigade.Status.free)
					.toList();

			List<Request> activeRequests = requestService.findAll().stream()
					.filter(r -> r.getStatus() != Request.Status.Completed)
					.toList();

			model.addAttribute("freeBrigades", freeBrigades);
			model.addAttribute("activeRequests", activeRequests);
			return "assignment/new";
		}

		try {
			assignmentService.assignBrigade(form.getRequestId(), form.getBrigadeId());
			return "redirect:/requests/" + form.getRequestId();
		} catch (IllegalArgumentException e) {

			List<Brigade> freeBrigades = brigadeService.findAll().stream()
					.filter(b -> b.getStatus() == Brigade.Status.free)
					.toList();

			List<Request> activeRequests = requestService.findAll().stream()
					.filter(r -> r.getStatus() != Request.Status.Completed)
					.toList();

			model.addAttribute("freeBrigades", freeBrigades);
			model.addAttribute("activeRequests", activeRequests);
			model.addAttribute("error", e.getMessage());
			return "assignment/new";
		}
	}

	/// Метод для отображения шаблона со всеми назначениями
	@GetMapping
	public String listAssignments(Model model) {
		List<Assignment> assignments = assignmentService.findAll();
		model.addAttribute("assignments", assignments);
		return "assignment/list";
	}

	/// Метод для отображения отдельного шаблона
	@GetMapping("/{id}")
	public String viewAssignment(@PathVariable Integer id, Model model) {
		Assignment assignment = assignmentService.findById(id);
		model.addAttribute("assignment", assignment);
		return "assignment/view";
	}

	/// Метод для отображения шаблона изменения назначения
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Integer id, Model model) {
		Assignment assignment = assignmentService.findById(id);

		AssignmentUpdateForm form = new AssignmentUpdateForm(assignment);
		model.addAttribute("assignmentForm", form);
		model.addAttribute("assignment", assignment);
		return "assignment/edit";
	}

	/// Обработка и занесение данных при изменении
	@PostMapping("/{id}")
	public String updateAssignment(@PathVariable Integer id,
			@Valid @ModelAttribute("assignmentForm") AssignmentUpdateForm form,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			Assignment assignment = assignmentService.findById(id);
			model.addAttribute("assignment", assignment);
			return "assignment/edit";
		}

		try {
			assignmentService.update(id, form);
			return "redirect:/assignments/" + id;
		} catch (IllegalArgumentException e) {
			Assignment assignment = assignmentService.findById(id);
			model.addAttribute("assignment", assignment);
			model.addAttribute("error", e.getMessage());
			return "assignment/edit";
		}
	}

	/// Метод для запроса на удаления назначения
	@PostMapping("/{id}/delete")
	public String deleteAssignment(@PathVariable Integer id) {
		Assignment assignment = assignmentService.findById(id);
		Integer requestId = assignment.getRequest().getRequestId();

		assignmentService.delete(id);
		return "redirect:/requests/" + requestId;
	}

	/// Метод для запроса на установку времени начала работ
	@PostMapping("/{id}/start")
	public String startWork(@PathVariable Integer id) {
		try {
			assignmentService.startWork(id);
			return "redirect:/assignments/" + id;
		} catch (IllegalArgumentException e) {
			return "redirect:/assignments/" + id + "?error=" + e.getMessage();
		}
	}

	/// Метод для запроса на установку времени окончания работ
	@PostMapping("/{id}/complete")
	public String completeWork(@PathVariable Integer id) {
		try {
			assignmentService.completeWork(id);
			return "redirect:/assignments/" + id;
		} catch (IllegalArgumentException e) {
			return "redirect:/assignments/" + id + "?error=" + e.getMessage();
		}
	}

//	@GetMapping("/request/{requestId}")
//	public String listByRequest(@PathVariable Integer requestId, Model model) {
//		List<Assignment> assignments = assignmentService.findAll().stream()
//				.filter(a -> a.getRequest().getRequestId().equals(requestId))
//				.toList();
//
//		Request request = requestService.findById(requestId);
//		model.addAttribute("assignments", assignments);
//		model.addAttribute("request", request);
//		return "assignment/list-by-request";
//	}
//
//	@GetMapping("/brigade/{brigadeId}")
//	public String listByBrigade(@PathVariable Integer brigadeId, Model model) {
//		List<Assignment> assignments = assignmentService.findAll().stream()
//				.filter(a -> a.getBrigade().getBrigadeId().equals(brigadeId))
//				.toList();
//
//		Brigade brigade = brigadeService.findById(brigadeId);
//		model.addAttribute("assignments", assignments);
//		model.addAttribute("brigade", brigade);
//		return "assignment/list-by-brigade";
//	}

}
