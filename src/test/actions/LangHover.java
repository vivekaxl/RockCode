package test.actions;

import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;


public class LangHover implements IJavaEditorTextHover
{
	@Override
	public String getHoverInfo(ITextViewer textviewer, IRegion region)
	{
		String newText1=SampleAction.newText1;
		System.out.println("Length of the String is :: "+newText1.length());
		System.out.println(newText1);
        return SampleAction.getPossibleNames(newText1.substring(region.getOffset(), region.getOffset()+region.getLength()));
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEditor(IEditorPart arg0) {
		// TODO Auto-generated method stub

	}
}
