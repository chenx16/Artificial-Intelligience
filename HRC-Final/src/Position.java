
/** 
 * DO NOT MODIFY.
 * @author Michael Wollowski
 */
public class Position {
		public int row;  // Make private and add setters and getters MIW
		public int col;  // Make private and add setters and getters MIW
		
		Position(int row, int col){
			this.row = row;
			this.col = col;
		}

		public int getCol() {
			// TODO Auto-generated method stub
			return this.col;
		}

		public int getRow() {
			// TODO Auto-generated method stub
			return this.row;
		}

		public void setCol(int col2) {
			// TODO Auto-generated method stub
			this.col=col2;
		}
		

		public void setRow(int row) {
			// TODO Auto-generated method stub
			this.row=row;		}
	}