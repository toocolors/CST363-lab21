package application;

import application.model.*;
import application.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import view.PrescriptionView;

import java.time.LocalDate;
import java.util.ArrayList;

@Controller
public class ControllerPrescriptionCreate {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private SequenceService sequence;

    @GetMapping("/prescription/new")
    public String getPrescriptionForm(Model model) {
        model.addAttribute("prescription", new PrescriptionView());
        return "prescription_create";
    }

    @PostMapping("/prescription")
    public String createPrescription(PrescriptionView p, Model model) {

        // Validate doctor
        Doctor doctor = doctorRepository.findByLastName(p.getDoctorLastName());
        if (doctor == null) {
            model.addAttribute("message", "Doctor not found.");
            model.addAttribute("prescription", p);
            return "prescription_create";
        }

        // Validate patient
        Patient patient = patientRepository.findByLastName(p.getPatientLastName());
        if (patient == null) {
            model.addAttribute("message", "Patient not found.");
            model.addAttribute("prescription", p);
            return "prescription_create";
        }

        // Validate drug
        Drug drug = drugRepository.findByName(p.getDrugName());
        if (drug == null) {
            model.addAttribute("message", "Drug not found.");
            model.addAttribute("prescription", p);
            return "prescription_create";
        }

        // Create prescription
        int id = sequence.getNextSequence("RXID_SEQUENCE");
        Prescription rx = new Prescription();
        rx.setRxid(id);  // fixed name
        rx.setPatientId(patient.getId());  // fixed name
        rx.setDoctorId(doctor.getId());    // fixed name
        rx.setDrugName(p.getDrugName());
        rx.setQuantity(p.getQuantity());
        rx.setDateCreated(LocalDate.now().toString());
        rx.setRefills(p.getRefills());
        rx.setFills(new ArrayList<>());  // empty fill list

        prescriptionRepository.insert(rx);

        p.setRxid(id);
        model.addAttribute("message", "Prescription created.");
        model.addAttribute("prescription", p);
        return "prescription_show";
    }
}