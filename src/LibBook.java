import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class LibBook {
	
	static
	{
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	
	Scanner sc = new Scanner(System.in);

	public void showAdminMenu()
	{
		System.out.println("─────[메뉴 선택]─────");
		System.out.println("1.     책 등록     ");
		System.out.println("2.     책 검색      ");
		System.out.println("3. 전체 책 리스트 조회  ");
		System.out.println("4.    대출 처리  ");
		System.out.println("5.    반납 처리  ");
		System.out.println("6.    회원 가입    ");
		System.out.println("7.  반납 연장 처리    ");
		System.out.println("8.   낡은 책 버리기    ");
		System.out.println("9.      종료    ");
		System.out.println("────────────────────");
		System.out.print("     선택 : ");
		
	}

	Connection con = null;
	PreparedStatement pstmt1 = null;
	PreparedStatement pstmt2 = null;
	PreparedStatement pstmt3 = null;
	PreparedStatement pstmt4 = null;
	PreparedStatement pstmt5 = null;
	PreparedStatement pstmt6 = null;
	PreparedStatement pstmt7 = null;
	PreparedStatement pstmt8 = null;
	PreparedStatement pstmt9 = null;
	PreparedStatement pstmt_tot = null;
	PreparedStatement pstmt_tat = null;

	ResultSet  rs  = null;
	
	public static void main(String[] args) {
		LibBook lib = new LibBook();
		lib.doLibRun();
	}
	
	public void doLibRun()
	{
		connectDB();
		choiceMenu();
	}
	
	public void choiceMenu() {
		connectDB();
		int choice;
		while(true) {
			showAdminMenu();
			choice = sc.nextInt();
			sc.nextLine();
			switch(choice) {
			case 1:
				addBook();
				break;
			case 2:
				bookInfo();
				break;
			case 3:
				bookList();
				break;
			case 4:
				bookRent();
				break;
			case 5:	
				backBook();
				break;
			case 6:
				signUp();
				break;
			case 7:
				extBook();
				break;
			case 8:
				delBook();
				break;
			case 9:
				disconnectDB();
				System.out.println("프로그램을 종료합니다.");
				System.out.println("이용해주셔서 감사합니다.");
				return;
			default:
				System.out.println("잘못 입력하셨습니다. 1~9 중에 고르세요.");
				break;	
			}
		}
	}
	
	public void addBook()
	{

		System.out.println("");
		System.out.print("제목 : ");
		String bookName = sc.next();
		System.out.print("권수 : ");
		int volume1 = sc.nextInt();

		try
		{

			String sql = "insert into book_db (b_no, b_title, b_volume) values (add_no.nextval, ?, ?)";
			pstmt1 = con.prepareStatement(sql);
//			pstmt1.setInt(1, max_no);
			pstmt1.setString(1, bookName);
			pstmt1.setInt(2, volume1);
			int updateCount = pstmt1.executeUpdate();
			//System.out.println("InsertCount : " + updateCount);
		if(updateCount == 1) {
			System.out.println("데이터가 정상적으로 추가되었습니다.");
		}else {
			System.out.println("데이터 입력에 실패했습니다.(#추가오류)");
		}
//		System.out.println("insertCount : " + updateCount);
	
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("데이터 입력에 실패했습니다.(#데이터예외)");
		}
	}
	
	public void bookInfo()
	{
		System.out.print("조회할 책 제목 : ");
		String bookName = sc.nextLine();
		try{	
			String sql = "select b_no as no, b_title as title, b_volume as volume from book_db where b_title = ?";
			pstmt2 = con.prepareStatement(sql);
			pstmt2.setString(1, bookName);
			rs = pstmt2.executeQuery();
			int nResult = 0;
			while(rs.next()) {
				nResult++;
				System.out.println("책번호 : " + rs.getString("no"));
				System.out.println("제  목 : " + rs.getString("title"));
				System.out.println("수  량 : " + rs.getInt("volume"));
				System.out.println("----------------------------------------");
			}
			if (nResult == 0)
			{
				System.out.println("조회할 데이터가 없습니다.");
				System.out.println("----------------------------------------");
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("데이터 입력에 실패했습니다.(#3)");
		}
	}
	
	public void bookList()
	{
		try{	
			String sql = "select * from book_db order by b_title";
			pstmt3 = con.prepareStatement(sql);
			rs = pstmt3.executeQuery();
		
			while(rs.next()) {
				System.out.println("책번호 : "+rs.getString("b_no"));
				System.out.println("제  목 : "+rs.getString("b_title"));
				System.out.println("수  량 : " + rs.getInt("b_volume"));
				System.out.println("----------------------------------------");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void bookRent()
	{
		System.out.print("회원 ID를 입력하세요 : ");
		String rent_id = sc.nextLine();
			try{	

				String sql = "select m_id, m_black_yn from member_db where m_id = ?";
				pstmt4 = con.prepareStatement(sql);
				pstmt4.setString(1, rent_id);
				rs = pstmt4.executeQuery();
				
				int nResult = 0;
				while(rs.next()) {
					nResult++;
					String id = rs.getString("m_id");
					String black_yn = rs.getString("m_black_yn");
					
					System.out.println(id);
					System.out.println(black_yn);
					
					if (black_yn.equals("Y")) {
						System.out.println("블랙리스트 입니다.");
						break;
					} else
					{
						LocalDate sysdate = LocalDate.now();
						LocalDate rentdate = sysdate;
						LocalDate backdate = sysdate.plusWeeks(1);
						//String rentday = "TO_DATE(SYSDATE)";
						//String backday = "TO_DATE(SYSDATE+7)";
						System.out.print("대여할 도서의 번호를 입력하세요 : ");
						int booknum = sc.nextInt();
						System.out.print("대여할 도서의 권 수를 입력하세요 : ");
						int volume2 = sc.nextInt();
					try
					{

						sql = "insert into rent_db(r_no, r_book_no, r_volume, rent_m_id, r_date, rt_date) "
								+ "values(rent_no.nextval, ?, ?, ?, sysdate, sysdate + 7)";
						pstmt4 = con.prepareStatement(sql);
						pstmt4.setInt(1, booknum);
						pstmt4.setInt(2, volume2);
						pstmt4.setString(3, id);
						System.out.println("");
						
						System.out.println("대여가 정상적으로 완료되었습니다(대여일, 반납일)");
						int updateCount = pstmt4.executeUpdate();
						//System.out.println("InsertCount : " + updateCount);
						
					}catch(Exception e)
					{
						e.printStackTrace();
						System.out.println("입력에 실패했습니다.(#4)");
					}
					
					try{	
						String sql1 = "update book_db set b_volume = b_volume - ? where b_no = ?";
						pstmt4 = con.prepareStatement(sql1);
						
						pstmt4.setInt(1, volume2);
						pstmt4.setInt(2, booknum);
						
						int updateCount = pstmt4.executeUpdate();
						System.out.println("도서 수량을 차감했습니다.");
//						System.out.println("DropCount : " + updateCount);
					}catch(Exception e) {
						System.out.println("데이터베이스 삭제 에러입니다.");
						e.printStackTrace();
					}
					
				} //if (black_yn.equals("Y")) {
					
				if (nResult == 0){
					System.out.println("회원 정보가 일치하지 않습니다.");
				}
			}  //while(rs.next()) {
		}  //try{
		catch(Exception e) {
			System.out.println("입력에 실패했습니다.(#3)");
			e.printStackTrace();
		}
	}
	
	public void backBook() {
		System.out.print("회원 ID를 입력해주세요 : ");
		String rent_id = sc.nextLine();
			try{	

				    String sql = "select m_id, m_black_yn from member_db where m_id = ?";
					pstmt5 = con.prepareStatement(sql);
					pstmt5.setString(1, rent_id);
					rs = pstmt5.executeQuery();
					int nResult = 0;
					while(rs.next()) {
						nResult++;
						String id = rs.getString("m_id");
						System.out.print("반납할 책 번호를 입력하세요 : ");
						int booknum = sc.nextInt();

						String sql_cnt = "select r_volume as book_cnt from rent_db where r_book_no = ? and rent_m_id =? ";
						pstmt_tot = con.prepareStatement(sql_cnt);
						pstmt_tot.setInt(1, booknum);
						pstmt_tot.setString(2, id);
						rs = pstmt_tot.executeQuery();
						
						int book_cnt = 0; 
						while(rs.next()) {
							book_cnt = Integer.parseInt(rs.getString("book_cnt"));
						}					
	    	
				    	try {
				    		sql = "delete from rent_db where r_book_no = ? and rent_m_id =? ";
				    		pstmt5 = con.prepareStatement(sql);
							pstmt5.setInt(1, booknum);
							pstmt5.setString(2, id);
							System.out.println("반납이 정상처리 되었습니다." );
							
							int updateCount = pstmt5.executeUpdate();
//							System.out.println("DropCount : " + updateCount);
							
				    	}catch(Exception e) {
				    		System.out.println("반납 에러입니다." );
				    		e.printStackTrace();
				    	}	
			    		try{	
							String sql1 = "update book_db set b_volume = b_volume + ? where b_no = ?";
							pstmt5 = con.prepareStatement(sql1);
							pstmt5.setInt(1, book_cnt);
							pstmt5.setInt(2, booknum);
							
							int updateCount = pstmt5.executeUpdate();
//							System.out.println("insertCount : " + updateCount);
						}catch(Exception e) {
							System.out.println("데이터베이스 삭제 에러입니다.");
							e.printStackTrace();
						}
			    		
		    	}if (nResult == 0){
					System.out.println("회원 정보가 일치하지 않습니다.");
				}
			
		}catch(Exception e) {
			
			System.out.println("입력에 실패했습니다.(#3)");
			e.printStackTrace();
		}	
	}
	
	public void signUp()
	{
		System.out.print("ID : ");
		String id = sc.nextLine();
			try{	
			String sql = "select m_id as id from member_db where m_id = ?";
			pstmt6 = con.prepareStatement(sql);
			pstmt6.setString(1, id);
			rs = pstmt6.executeQuery();
			int nResult = 0;
			while(rs.next()) {
				nResult++;
				rs.getString("id");
				System.out.println(rs.getString("id")+"는 이미 사용중인 ID입니다.");
				System.out.println("");
			}
			if (nResult == 0)
			{
				System.out.println("["+id+"]"+"는 사용 가능한 ID입니다.");
				System.out.print("성함 : ");
				String name = sc.nextLine();
				try
				{

					String sql_cnt = "select NVL(MAX(m_no),0) as max_no from member_db";
					pstmt_tot = con.prepareStatement(sql_cnt);
					rs = pstmt_tot.executeQuery();
					
					int max_no = 0;  //대여 db 레코드 카운트
					while(rs.next()) {
						 max_no = Integer.parseInt(rs.getString("max_no")) + 1;
					}					
					
					String sql2 = "insert into member_db (m_no, m_id, m_name) values(?, ?, ?)";
					pstmt6 = con.prepareStatement(sql2);
					pstmt6.setInt(1, max_no);
					pstmt6.setString(2, id);
					pstmt6.setString(3, name);

					int creCount = pstmt6.executeUpdate();
				if(creCount == 1) {
					System.out.println("["+id+"]님의 "+"회원가입이 정상적으로 처리 되었습니다.");
				}else {
					System.out.println("회원가입 데이터 입력에 실패했습니다.(#가입오류)");
				}
				//System.out.println("insertCount : " + updateCount);
			
				}catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("데이터 입력에 실패했습니다.(#데이터예외)");
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("입력에 실패했습니다.(#3)");
		}	
	}
	public void extBook()
	{
		System.out.print("회원 ID를 입력하세요 : ");
		String rent_id = sc.nextLine();
		System.out.print("대여할 도서의 번호를 입력하세요 : ");
		int booknum = sc.nextInt();

	
		try{	
			String sql = "select m_id, m_black_yn from member_db where m_id = ?";
			pstmt4 = con.prepareStatement(sql);
			pstmt4.setString(1, rent_id);
			rs = pstmt4.executeQuery();
			
			int nResult = 0;
			while(rs.next()) {
				nResult++;
			}
			if (nResult == 0){
				System.out.println("회원 정보가 일치하지 않습니다.");
			}		
			
			
			String sql1 = "update rent_db set rt_date = rt_date + 7  where r_book_no = ?"
					+ "and rent_m_id = ?";
			pstmt4 = con.prepareStatement(sql1);
			
			pstmt4.setInt(1, booknum);
			pstmt4.setString(2, rent_id);
			
			int updateCount = pstmt4.executeUpdate();
			System.out.println("반납 일자를 7일 연장 했습니다.");
//			System.out.println("DropCount : " + updateCount);
		}catch(Exception e) {
			System.out.println("데이터베이스 삭제 에러입니다.");
			e.printStackTrace();
		}

	}
	
	public void delBook()
	{
		System.out.print("삭제할 책 제목 : ");
		String bookName = sc.nextLine();
		
		try{	
			String sql = "update book_db set b_volume = b_volume - 1 where b_title = ?";
			pstmt8 = con.prepareStatement(sql);
			pstmt8.setString(1, bookName);
			
			int updateCount = pstmt8.executeUpdate();
			System.out.println("데이터베이스에서 삭제되었습니다.");
//			System.out.println("DropCount : " + updateCount);
		}catch(Exception e) {
			System.out.println("데이터베이스 삭제 에러입니다.");
			e.printStackTrace();
		}
	}
	
	public void connectDB() {
		try 
		{
			con = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:xe",
					"study",
					"1234");
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
//				if(con != null) con.close();
			}catch(Exception sqle) { 
				sqle.printStackTrace();
			}
		}
	}	
	
	public void disconnectDB() {
		try
		{
			if(rs != null) rs.close();
		
			if(con != null) con.close();
		}catch(Exception e) {
			
		}
	}
}
