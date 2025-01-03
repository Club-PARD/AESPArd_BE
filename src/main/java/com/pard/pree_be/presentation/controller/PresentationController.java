package com.pard.pree_be.presentation.controller;

import com.pard.pree_be.practice.dto.PracticeRequestDto;
import com.pard.pree_be.practice.entity.Practice;
import com.pard.pree_be.presentation.dto.PresentationCellDto;
import com.pard.pree_be.presentation.dto.PresentationRequestDto;
import com.pard.pree_be.presentation.dto.PresentationCellDto;
import com.pard.pree_be.presentation.dto.PresentationResponseDto;
import com.pard.pree_be.presentation.entity.Presentation;
import com.pard.pree_be.presentation.service.PresentationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/presentations")
@RequiredArgsConstructor
public class PresentationController {

        private final PresentationService presentationService;

        @PostMapping("/create-presentation")
        @Operation(summary = "새로운 발표 생성 ( AddPresentation )  ✅", description = "새로운 발표를 생성합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "발표가 성공적으로 생성되었습니다."),
                        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터입니다.")
        })
        public ResponseEntity<PresentationResponseDto> createPresentation(@RequestBody @Valid PresentationRequestDto requestDto) {
                Presentation presentation = presentationService.createPresentation(requestDto);
                PresentationResponseDto responseDto = PresentationResponseDto.fromEntity(presentation);
                return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        }

        @GetMapping("/user/{userId}/modal-list")
        @Operation(summary = "발표 리스트 가져오기  ✅", description = "특정 사용자의 발표 리스트를 반환합니다. 각 발표에는 이름, 총 연습 횟수, 포맷된 업데이트 날짜, 최소/최대 시간이 포함됩니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "발표 리스트를 성공적으로 가져왔습니다."),
                        @ApiResponse(responseCode = "404", description = "사용자 데이터를 찾을 수 없습니다.")
        })
        public ResponseEntity<List<PresentationCellDto>> getPresentationList(@PathVariable UUID userId) {
                List<PresentationCellDto> presentations = presentationService.getPresentationList(userId);
                return ResponseEntity.ok(presentations);
        }

        @Operation(summary = "최신순 발표 리스트 가져오기 ( Home ) ✅", description = "특정 사용자의 최신순으로 정렬된 발표 리스트를 반환합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "발표 리스트를 성공적으로 가져왔습니다."),
                        @ApiResponse(responseCode = "404", description = "사용자 데이터를 찾을 수 없습니다.")
        })
        @GetMapping("/user/{userId}/latest")
        public ResponseEntity<List<PresentationCellDto>> getPresentationsByUpdatedAt(@PathVariable UUID userId) {
                List<PresentationCellDto> presentations = presentationService.getPresentationsSortedByUpdatedAt(userId);
                return ResponseEntity.ok(presentations);
        }

        @Operation(summary = "즐겨찾기 순 발표 리스트 가져오기 ( Home ) ✅", description = "특정 사용자의 즐겨찾기와 최신순으로 정렬된 발표 리스트를 반환합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "발표 리스트를 성공적으로 가져왔습니다."),
                        @ApiResponse(responseCode = "404", description = "사용자 데이터를 찾을 수 없습니다.")
        })
        @GetMapping("/user/{userId}/favorites")
        public ResponseEntity<List<PresentationCellDto>> getPresentationsByFavorite(@PathVariable UUID userId) {
                List<PresentationCellDto> presentations = presentationService.getFavoritesByCreatedAt(userId);
                return ResponseEntity.ok(presentations);
        }

        @PatchMapping("/{presentationId}/toggle-favorite")
        @Operation(summary = "발표 즐겨찾기 토클 ( Home ) ✅", description = "선택한 발표를 즐겨찾기로 설정하거나 해제합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "즐겨찾기 상태가 성공적으로 변경되었습니다."),
                @ApiResponse(responseCode = "404", description = "발표를 찾을 수 없습니다.")
        })
        public ResponseEntity<Boolean> toggleFavorite(@PathVariable UUID presentationId) {
                boolean newState = presentationService.toggleFavorite(presentationId);
                return ResponseEntity.ok(newState);
        }


        @Operation(summary = "발표 1개 삭제 : 테스트용 🤓👍", description = "입력한 발표 ID 에 해당하는 발표 삭제 ( 안에 모든파일도 같이 삭제)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "발표가 성공적으로 삭제되었습니다."),
                        @ApiResponse(responseCode = "404", description = "발표를 찾을 수 없습니다.")
        })
        @DeleteMapping("/{presentationId}/one-delete")
        public ResponseEntity<Void> deletePresentation(@PathVariable UUID presentationId) {
                presentationService.deletePresentation(presentationId);
                return ResponseEntity.noContent().build();
        }



        @Operation(summary = "특정 사용자의 **모든** 발표 삭제 ( My ) 🚨✅", description = "유저 안에 모든. 모오오오듬 발표 연습 싹 날라가니까 조심")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "모든 발표가 성공적으로 삭제되었습니다."),
                        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
        })
        @DeleteMapping("/{userId}/all-delete")
        public ResponseEntity<Void> deleteAllPresentationsForUser(@PathVariable UUID userId) {
                presentationService.deleteAllPresentationsForUser(userId);
                return ResponseEntity.noContent().build();
        }


        @Operation(summary = "선택한 모든 발표 삭제 ( Home )✅", description = "선택된 발표 ID 목록을 받아 해당 발표를 모두 삭제합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "선택된 발표가 성공적으로 삭제되었습니다."),
                        @ApiResponse(responseCode = "400", description = "발표 ID 목록이 비어 있습니다."),
                        @ApiResponse(responseCode = "404", description = "발표를 찾을 수 없습니다.")
        })
        @DeleteMapping("/batch-delete")
        public ResponseEntity<Void> deleteSelectedPresentations(@RequestBody List<UUID> presentationIds) {
                presentationService.deleteSelectedPresentations(presentationIds);
                return ResponseEntity.noContent().build();
        }


        @GetMapping("/search/{userId}")
        @Operation(summary = "검색 기능 !! ( Search ) ✅", description = "기본 정렬은 시간순이고, 빈키워드는 다 불러옴 !")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "성공적으로 불러옴~"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청!")
        })
        public ResponseEntity<List<PresentationCellDto>> searchPresentations(
                @PathVariable UUID userId,
                @RequestParam(required = false) String searchTerm
        ) {
                List<PresentationCellDto> presentations = presentationService.searchPresentationsByName(userId, searchTerm);
                return ResponseEntity.ok(presentations);
        }

        @PatchMapping("/{presentationId}/update-name")
        @Operation(summary = "발표 이름 수정 / 업데이트 하기")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Presentation name updated successfully."),
                @ApiResponse(responseCode = "404", description = "Presentation not found.")
        })
        public ResponseEntity<Void> updatePresentationName(@PathVariable UUID presentationId, @RequestBody String newName) {
                // Sanitize the name: remove quotes and trim whitespace
                String sanitizedNewName = newName.replaceAll("^\"|\"$", "").trim();
                presentationService.updatePresentationName(presentationId, sanitizedNewName);
                return ResponseEntity.ok().build();
        }




}
