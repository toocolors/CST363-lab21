package application;

import application.model.Doctor;
import application.model.DoctorRepository;
import application.model.Patient;
import application.model.PatientRepository;
import application.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import view.PatientView;

@Controller
public class ControllerPatientCreate {

  @Autowired
  PatientRepository patientRepository;

  @Autowired
  SequenceService sequence;

  @Autowired
  private DoctorRepository doctorRepository;

  /*
  * Request blank patient registration form.
  */
  @GetMapping("/patient/new")
  public String getNewPatientForm(Model model) {
    // return blank form for new patient registration
    model.addAttribute("patient", new PatientView());
    return "patient_register";
  }

  /*
  * Process data from the patient_register form
  */
  @PostMapping("/patient/new")
  public String createPatient(PatientView p, Model model) {
    /*
     * validate doctor last name and find the doctor id
    */
    Doctor d = doctorRepository.findByLastName(p.getPrimaryName());
    if (d == null) {
      model.addAttribute("message", "Doctor not found.");
      model.addAttribute("patient", p);
      return "patient_register";
    }

    /*
     * insert to patient collection
    */
    // get the next unique id for patient
    int id = sequence.getNextSequence("PATIENT_SEQUENCE");
    // create a model.patient instance
    // copy data from PatientView to model
    Patient patient = new Patient();
    patient.setId(id);
    patient.setLastName(p.getLastName());
    patient.setFirstName(p.getFirstName());
    patient.setBirthdate(p.getBirthdate());
    patient.setSsn(p.getSsn());
    patient.setStreet(p.getStreet());
    patient.setCity(p.getCity());
    patient.setState(p.getState());
    patient.setZipcode(p.getZipcode());
    patient.setPrimaryName(p.getPrimaryName());
    patientRepository.save(patient);

    // Display message and patient information
    model.addAttribute("message", "Patient created.");
    p.setId(id);
    model.addAttribute("patient", p);
    return "patient_show";
  }

  /*
   * Request blank form to search for patient by id and name
  */
  @GetMapping("/patient/get")
  public String getSearchForm(Model model) {
    model.addAttribute("patient", new PatientView());
    return "patient_get";
  }

  /*
   * Perform search for patient by patient id and name.
  */
  @PostMapping("/patient/get")
  public String showPatient(PatientView p, Model model) {
    /*
     * search for patient by id and name
    */
    // Retrieve patient using the id, last_name entered by user
    Patient patient = patientRepository.findByIdAndLastName(p.getId(),p.getLastName());
    if(patient != null) {
      // copy data from model to view
      p.setId(patient.getId());
      p.setLastName(patient.getLastName());
      p.setFirstName(patient.getFirstName());
      p.setBirthdate(patient.getBirthdate());
      p.setSsn(patient.getSsn());
      p.setStreet(patient.getStreet());
      p.setCity(patient.getCity());
      p.setState(patient.getState());
      p.setZipcode(patient.getZipcode());
      p.setPrimaryName(patient.getPrimaryName());

      // Save p in model
      model.addAttribute("message", "Patient found.");
      model.addAttribute("patient", p);
      return "patient_show";
    } else {
      model.addAttribute("message", "Patient not found.");
      model.addAttribute("patient", p);
      return "patient_get";
    }
  }
}
