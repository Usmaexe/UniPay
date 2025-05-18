package com.unipay.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * BaseEntity serves as a foundational class for all JPA entities, encapsulating common auditing fields.
 * It leverages Spring Data JPA's auditing capabilities to automatically populate metadata such as creation
 * and modification timestamps, versioning for optimistic locking, and a soft delete flag.
 *
 * <p>Key Features:
 * <ul>
 *   <li><strong>id</strong>: Primary key identifier, auto-generated.</li>
 *   <li><strong>createdAt</strong>: Timestamp marking when the entity was created.</li>
 *   <li><strong>updatedAt</strong>: Timestamp marking the last update to the entity.</li>
 *   <li><strong>version</strong>: Version number for optimistic locking to prevent concurrent update issues.</li>
 *   <li><strong>isDeleted</strong>: Boolean flag indicating soft deletion status.</li>
 * </ul>
 *
 * <p>Annotations Used:
 * <ul>
 *   <li><strong>@MappedSuperclass</strong>: Indicates that this class provides mapping information for its subclasses.</li>
 *   <li><strong>@EntityListeners(AuditingEntityListener.class)</strong>: Enables auditing by listening to entity lifecycle events.</li>
 *   <li><strong>@CreatedDate</strong> and <strong>@LastModifiedDate</strong>: Automatically manage timestamp fields.</li>
 *   <li><strong>@Version</strong>: Facilitates optimistic locking by tracking entity versions.</li>
 * </ul>
 *
 * <p>Note: To activate auditing features, ensure that your Spring Boot application is configured with
 * <strong>@EnableJpaAuditing</strong> and an appropriate implementation of <strong>AuditorAware</strong> if you plan to use
 * <strong>@CreatedBy</strong> and <strong>@LastModifiedBy</strong> annotations for tracking user information.
 *
 * @see org.springframework.data.annotation.CreatedDate
 * @see org.springframework.data.annotation.LastModifiedDate
 * @see org.springframework.data.annotation.Version
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
@Getter
@Setter
@EqualsAndHashCode
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * The unique identifier for this entity. This value is auto-generated using the UUID2 strategy.
     * This serves as the primary key for the entity.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    protected String id;

    /**
     * The timestamp when the entity was created. This value is automatically set by Spring Data JPA
     * during the entity's creation.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp when the entity was last updated. This value is automatically set by Spring Data JPA
     * whenever the entity is updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * The version number for this entity, used for optimistic locking.
     * This helps prevent concurrent modification issues.
     */
    @Version
    private Integer version;

    /**
     * A boolean flag indicating whether the entity has been logically deleted (soft delete).
     * This is useful for preserving historical data and marking records as deleted without removing them from the database.
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}
