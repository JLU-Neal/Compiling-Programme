import java.io.*;
import java.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import mycompiler.cifa.*;
import mycompiler.yufa.*;

public class compiler extends Applet implements ActionListener
{
    Button button1,button2;
    Label label1,label2;
    TextArea text1;
    MyWindow window1,window2,window3,window4;
    GridLayout net;

    public void init()
    {
	net=new GridLayout(1,2);
        setLayout(net);

	text1=new TextArea("",100,400);
        Panel p1=new Panel();
	p1.setLayout(new GridLayout(1,1));
        p1.add(text1);
        add(p1);

	label1=new Label("在左边输入程序，按键看结果");
	button1=new Button("词法分析");
	button2=new Button("语法分析");
        Panel p2=new Panel();
	p2.setLayout(new GridLayout(8,2));
        p2.add(label1);    p2.add(new Label());        
        p2.add(button1);   p2.add(button2);        
        p2.add(new Label());
        add(p2);

	window1=new MyWindow("词法分析");
	window2=new MyWindow("语法树");
	window3=new MyWindow("语法错误信息");
	window4=new MyWindow("符号表");

	button1.addActionListener(this);
	button2.addActionListener(this);
    }
    public void actionPerformed(ActionEvent e)
    {
	if (e.getSource()==button1)
	{
           String s=text1.getText();
           CreatToken ct=new CreatToken(s); 
           s=ct.tok; 
           s=s.trim();         
           window1.text.setText(s);
	   window1.setVisible(true); 
	}
        else if (e.getSource()==button2)
	{
           String s=text1.getText();
           CreatToken ct=new CreatToken(s); 
           s=ct.tok; 
           s=s.trim();
           Recursion r=new Recursion(s);
           if (r.Error)
           {
               window3.text.setText(r.serror);
	       window3.setVisible(true);
           } 
           else
           {
               window2.text.setText(r.stree);
	       window2.setVisible(true);
           } 
	}
        
    }
}
class MyWindow extends Frame
{
    TextArea text;
    MyWindow(String s)
    {
	super(s);
	setLayout(new GridLayout(1,1));
        text=new TextArea("",100,300);
        add(text);
	setVisible(false);
	pack();
        addWindowListener(new WindowAdapter() 
        {
            public void windowClosing(WindowEvent e)
            {setVisible(false); System.exit(0);}  
        });
    }
}