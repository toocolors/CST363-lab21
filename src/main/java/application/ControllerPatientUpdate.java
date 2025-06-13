package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import view.PatientView;
import application.model.Patient;
import application.model.Doctor;
import application.model.PatientRepository;
import application.model.DoctorRepository;

import java.util.Optional;

@Controller
public class ControllerPatientUpdate {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // Display edit form
    @GetMapping("/patient/edit/{id}")
    public String getUpdateForm(@PathVariable int id, Model model) {
        Optional<Patient> opt = patientRepository.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("message", "Patient not found.");
            return "index";
        }

        Patient p = opt.get();

        PatientView pv = new PatientView();
        pv.setId(p.getId());
        pv.setFirstName(p.getFirstName());
        pv.setLastName(p.getLastName());
        pv.setBirthdate(p.getBirthdate());
        pv.setSsn(p.getSsn());
        pv.setStreet(p.getStreet());
        pv.setCity(p.getCity());
        pv.setState(p.getState());
        pv.setZipcode(p.getZipcode());
        pv.setPrimaryName(p.getPrimaryName());

        model.addAttribute("patient", pv);
        model.addAttribute("message", "Edit patient details below:");
        return "patient_edit";
    }

    // Process form submission
    @PostMapping("/patient/edit")
    public String updatePatient(PatientView p, Model model) {
        Doctor doctor = doctorRepository.findByLastName(p.getPrimaryName());
        if (doctor == null) {
            model.addAttribute("message", "Error: Doctor not found.");
            model.addAttribute("patient", p);
            return "patient_edit";
        }

        Optional<Patient> opt = patientRepository.findById(p.getId());
        if (opt.isEmpty()) {
            model.addAttribute("message", "Patient not found.");
            return "index";
        }

        Patient patient = opt.get();
        patient.setFirstName(p.getFirstName());
        patient.setLastName(p.getLastName());
        patient.setSsn(p.getSsn());
        patient.setBirthdate(p.getBirthdate());
        patient.setStreet(p.getStreet());
        patient.setCity(p.getCity());
        patient.setState(p.getState());
        patient.setZipcode(p.getZipcode());
        patient.setPrimaryName(doctor.getLastName()); // Update doctor if changed

        patientRepository.save(patient);

        model.addAttribute("message", "Patient updated successfully.");
        model.addAttribute("patient", p);
        return "patient_show";
    }
}
