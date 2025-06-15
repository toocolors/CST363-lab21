package application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import application.model.*;
import application.service.*;
import view.*;

@Controller
public class ControllerPrescriptionFill {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PharmacyRepository pharmacyRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private DrugRepository drugRepository;

	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_fill";
	}

	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model) {

		try {
			Pharmacy pharmacy = null;
			for (Pharmacy ph : pharmacyRepository.findAll()) {
				if (ph.getName().equals(p.getPharmacyName())) {
					pharmacy = ph;
					break;
				}
			}

			if (pharmacy == null) {
				model.addAttribute("message", "The pharmacy '" + p.getPharmacyName() + "' is an invalid pharmacy name.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			p.setPharmacyID(pharmacy.getId());
			p.setPharmacyPhone(pharmacy.getPhone());
			p.setPharmacyAddress(pharmacy.getAddress());

			Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(p.getRxid());
			if (!prescriptionOpt.isPresent()) {
				model.addAttribute("message", "The following RXID '" + p.getRxid() + "' is an invalid prescription ID.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			Prescription prescription = prescriptionOpt.get();

			p.setPatientId(prescription.getPatientId());
			p.setDoctorId(prescription.getDoctorId());
			p.setDrugName(prescription.getDrugName());
			p.setQuantity(prescription.getQuantity());
			p.setRefills(prescription.getRefills());

			Optional<Patient> patientOpt = patientRepository.findById(prescription.getPatientId());
			if (patientOpt.isPresent()) {
				Patient patient = patientOpt.get();
				p.setPatientFirstName(patient.getFirstName());
				p.setPatientLastName(patient.getLastName());
			}

			int currentFills = prescription.getFills().size();
			int allowedRefills = prescription.getRefills();

			if (currentFills > allowedRefills) {
				model.addAttribute("message", "Prescription has exceeded allowed refills. " +
						"Current fills: " + currentFills + ", Allowed refills: " + allowedRefills);
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			Doctor doctor = doctorRepository.findById(prescription.getDoctorId());
			if (doctor != null) {
				p.setDoctorFirstName(doctor.getFirstName());
				p.setDoctorLastName(doctor.getLastName());
			}

			BigDecimal unitPrice = BigDecimal.ZERO;
			String drugName = prescription.getDrugName();

			for (Pharmacy.DrugCost drugCost : pharmacy.getDrugCosts()) {
				if (drugCost.getDrugName().equals(drugName)) {
					unitPrice = BigDecimal.valueOf(drugCost.getCost());
					break;
				}
			}

			if (unitPrice.equals(BigDecimal.ZERO)) {
				model.addAttribute("message", "Drug is not available at this pharmacy");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(prescription.getQuantity()));
			p.setCost(totalCost.toString());

			Prescription.FillRequest fillRequest = new Prescription.FillRequest();
			fillRequest.setPharmacyID(pharmacy.getId());
			fillRequest.setDateFilled(LocalDate.now().toString());
			fillRequest.setCost(totalCost.toString());

			prescription.getFills().add(fillRequest);

			prescriptionRepository.save(prescription);

			p.setRefillsRemaining(allowedRefills - (currentFills + 1));
			p.setDateFilled(LocalDate.now().toString());

		} catch (Exception e) {
			model.addAttribute("message", "Error processing prescription fill: " + e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}

		model.addAttribute("message", "Prescription filled.");
		model.addAttribute("prescription", p);
		return "prescription_show";
	}
}