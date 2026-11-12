package vn.slmglobal.slmai.services.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.media.entity.Topic;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, String> {

    List<Topic> findAllByOrderBySortOrderAscCodeAsc();
}
