package gr.aueb.cf.pharmapp_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing //ετσι μπορουμε να δινουμε στο model αυτοματα Timestamps
// (createdAt, updatedAt)
public class PharmApplication {

	public static void main(String[] args) {
		SpringApplication.run(PharmApplication.class, args);
	}

}
