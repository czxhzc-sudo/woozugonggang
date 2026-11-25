package com.example.teamjavaprogramming.repository;
import com.example.teamjavaprogramming.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post,Long>{
    // 작성자 이름으로 게시글 조회
    List<Post> findByAuthor(String author);
    // 특정 날짜 이전에 생성된 게시글 조회
    List<Post> findByCreateDateBefore(LocalDateTime cutoff);
}
