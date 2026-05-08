package core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import core.entities.enums.InvoiceType;
import core.entities.enums.PaymentMethod;
import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"invoiceLines", "parentInvoice"})
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedId(prefix = "INV", numberLength = 6, sequenceName = "seq_invoice_id")
    @Column(name = "invoice_id", length = 20, nullable = false)
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;
    private String note;
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff creator;
    @ManyToOne
    @JoinColumn(name = "shift_id")
    private Shift shift;
    @Column(name = "prescription_code")
    private String prescriptionCode;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.PERSIST)
    @JsonIgnore
    @JsonManagedReference("invoice-invoiceLines")
    private List<InvoiceLine> invoiceLines;
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    @OneToOne
    @JoinColumn(name = "referenced_invoice_id")
    private Invoice referencedInvoice;
}
