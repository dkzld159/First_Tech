package com.example.demo.controller.joontaek;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.BoardDto;
import com.example.demo.dto.BoardViewDto;
import com.example.demo.dto.CommentDto;
import com.example.demo.dto.SearchBoardEntity;
import com.example.demo.dto.UserDto;
import com.example.demo.service.IBoardService;
import com.example.demo.service.ICommentService;
import com.example.demo.vo.joontaek.BoardVo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/b")
public class BoardController {

	@Value("${app.page.amount}")
	private int amount = 5;
	@Value("${spring.servlet.multipart.location:./uploads}")
	private String uploadPath;

	@Autowired
	IBoardService boardService;

	@Autowired
	ICommentService commentService;

	@RequestMapping("/board/{pageNum}")
	public String board(@PathVariable("pageNum") int pageNum, Model model) {

		// 여기부터
		int startNum = pageNum * amount - amount;
		List<BoardViewDto> boardList = boardService.getBoardListPaging(startNum, amount);
		int totalCnt = boardService.getBoardCount();
		int endPageNum = Math.ceilDiv(totalCnt, amount);
		model.addAttribute("boardList", boardList);
		model.addAttribute("currentPageNum", pageNum);
		model.addAttribute("endPageNum", endPageNum);
		// 여기까지 게시판 목록

		return "/taek/board";
	}

	@RequestMapping("/boardDetail/{boardNum}")
	public String boardDetail(@PathVariable("boardNum") int boardNum, Model model) {

		BoardDto board = boardService.getBoard(boardNum);
		board.setViews(board.getViews() + 1); // 조회수 +1
		boardService.BoardViews(board.getViews(), boardNum);

		model.addAttribute("board", board);
		List<CommentDto> commentList = commentService.getCommentList(boardNum);
		model.addAttribute("commentList", commentList);
		return "/taek/boardDetail";
	}

	@RequestMapping("boardWriteForm")
	public String boardWriteForm(HttpServletRequest request,Model model) {
		
		
		HttpSession session = request.getSession();
		UserDto user =(UserDto)session.getAttribute("user");
		
		//아래는 게시판 보여줄때 무조건 필수임 ㅡㅡ
		int pageNum=1;
		int startNum = pageNum * amount - amount;
		int totalCnt = boardService.getBoardCount();
		int endPageNum = Math.ceilDiv(totalCnt, amount);
		List<BoardViewDto> boardList = boardService.getBoardListPaging(startNum, amount);
	
		
		
		if(user==null) {
			model.addAttribute("msg","");
			model.addAttribute("currentPageNum", pageNum);
			model.addAttribute("endPageNum", endPageNum);
			model.addAttribute("boardList", boardList);	
			return "/taek/board";
		}else {
		
		
		return "/taek/boardWriteForm";
		}
	}

	// ----------------------------파일업로드 vo용 테스트----------------------------------
	@RequestMapping("boardWrite")
	public String boardWriteTest(Model model, HttpServletRequest request, BoardVo vo) {

		// boardWriter 값 저장하기위에서 세션에 저장되있는 유저 객체 불러오기
		HttpSession session = request.getSession();

		UserDto user = (UserDto) session.getAttribute("user");
		String boardWriter = user.getUserNickname();

		MultipartFile file = vo.getUploadFileName();
		String fileName = file.getOriginalFilename();
		File uploadFile = new File(uploadPath + fileName);

		if (fileName.equals("")) {
			fileName = null;
		}

		try {
			file.transferTo(uploadFile);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		BoardDto board = new BoardDto();

		board.setBoardTitle(vo.getBoardTitle());
		board.setBoardContent(vo.getBoardContent());
		board.setBoardWriter(boardWriter);
		board.setFileName(fileName);

		boardService.regBoard(board);

		int pageNum = 1;
		int startNum = pageNum * amount - amount;
		List<BoardViewDto> boardList = boardService.getBoardListPaging(startNum, amount);
		int totalCnt = boardService.getBoardCount();
		int endPageNum = Math.ceilDiv(totalCnt, amount);

		model.addAttribute("boardList", boardList);
		model.addAttribute("currentPageNum", pageNum);
		model.addAttribute("endPageNum", endPageNum);

		return "/taek/board";
	}

	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓게시글 검색(AJAX_포기)↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	@RequestMapping("/boardSearch/{pageNum}")
	public String searchBoard(@ModelAttribute("SearchBoardEntity") SearchBoardEntity searchBoardEntity, Model model,
			@PathVariable("pageNum") int pageNum) {
		
		int startNum = pageNum * amount - amount;
		
		if(searchBoardEntity.getSearchInput().equals("")) {
			List<BoardViewDto> boardList = boardService.getBoardListPaging(startNum, amount);
			int totalCnt = boardService.getBoardCount();
			int endPageNum = Math.ceilDiv(totalCnt, amount);
			model.addAttribute("boardList", boardList);
			model.addAttribute("currentPageNum", pageNum);
			model.addAttribute("endPageNum", endPageNum);
			
		}
		
		if (searchBoardEntity.getSearchPart().equals("title")) {
			List<BoardViewDto> boardList = boardService.searchBoardByTitle(searchBoardEntity.getSearchInput(), startNum,
					amount);
			int totalCnt = boardService.searchBoardByTitleCount(searchBoardEntity.getSearchInput());
			int endPageNum = Math.ceilDiv(totalCnt, amount);

			model.addAttribute("currentPageNum", pageNum);
			model.addAttribute("endPageNum", endPageNum);
			model.addAttribute("boardList", boardList);
			model.addAttribute("searchBoardEntity", searchBoardEntity);
		}

		if (searchBoardEntity.getSearchPart().equals("content")) {

			List<BoardViewDto> boardList = boardService.searchBoardByContent(searchBoardEntity.getSearchInput(),
					startNum, amount);
			int totalCnt = boardService.searchBoardByContentCount(searchBoardEntity.getSearchInput());
			int endPageNum = Math.ceilDiv(totalCnt, amount);

			model.addAttribute("currentPageNum", pageNum);
			model.addAttribute("endPageNum", endPageNum);
			model.addAttribute("boardList", boardList);
			model.addAttribute("searchBoardEntity", searchBoardEntity);
		}

		if (searchBoardEntity.getSearchPart().equals("writer")) {
			List<BoardViewDto> boardList = boardService.searchBoardByWriter(searchBoardEntity.getSearchInput(),
					startNum, amount);
			int totalCnt = boardService.searchBoardByWriterCount(searchBoardEntity.getSearchInput());
			int endPageNum = Math.ceilDiv(totalCnt, amount);
			model.addAttribute("currentPageNum", pageNum);
			model.addAttribute("endPageNum", endPageNum);
			model.addAttribute("boardList", boardList);
			model.addAttribute("searchBoardEntity", searchBoardEntity);

			
		}
		return "/taek/searchBoard";
	}

	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓코멘트 관리↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓코멘트 관리↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓코멘트 관리↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	
	@RequestMapping("addComment")
	@ResponseBody
	public List<CommentDto> addComment(@RequestParam("content") String content, @RequestParam("boardNum") int boardNum,
			HttpServletRequest request, Model model) {
		HttpSession session = request.getSession();

		UserDto user = (UserDto) session.getAttribute("user");

		commentService.regComment(content, boardNum, user.getUserNickname());
		List<CommentDto> commentList = commentService.getCommentList(boardNum);
		model.addAttribute("comment", commentList);

		return commentService.getCommentList(boardNum);
	}

	@RequestMapping("getCommentList")
	@ResponseBody
	public List<CommentDto> getCommentList(@RequestParam("boardNum") int boardNum) {
		return commentService.getCommentList(boardNum);
	}


}
