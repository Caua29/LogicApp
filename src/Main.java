import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * LogicApp - Conjuntos e Lógica (Ensino Médio)
 * Dica: pressione F11 para alternar tela cheia durante a apresentação.
 */
public class Main {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new TelaInicial().setVisible(true));
    }
}

// ==========================================================
// PALETA DE CORES E FONTES
// ==========================================================
class Cores {
    public static final Color FUNDO = new Color(13, 15, 26);
    public static final Color MENU_TOPO = new Color(26, 29, 50);
    public static final Color MENU_BASE = new Color(14, 16, 28);
    public static final Color CARTAO = new Color(27, 31, 50);
    public static final Color CARTAO_CLARO = new Color(40, 46, 72);
    public static final Color BORDA = new Color(58, 65, 98);

    public static final Color AZUL = new Color(96, 125, 255);
    public static final Color ROXO = new Color(166, 120, 255);
    public static final Color CIANO = new Color(56, 207, 232);
    public static final Color VERDE = new Color(52, 211, 120);
    public static final Color VERMELHO = new Color(245, 95, 100);
    public static final Color AMARELO = new Color(255, 200, 70);
    public static final Color LARANJA = new Color(255, 150, 60);

    public static final Color TEXTO = new Color(240, 243, 255);
    public static final Color TEXTO_SECUNDARIO = new Color(158, 166, 192);

    public static final Font TITULO_GRANDE = new Font("Dialog", Font.BOLD, 42);
    public static final Font TITULO = new Font("Dialog", Font.BOLD, 32);
    public static final Font SUBTITULO = new Font("Dialog", Font.BOLD, 20);
    public static final Font TEXTO_NORMAL = new Font("Dialog", Font.PLAIN, 17);
    public static final Font TEXTO_PEQUENO = new Font("Dialog", Font.PLAIN, 14);
    public static final Font ETIQUETA = new Font("Dialog", Font.BOLD, 13);

    static Color misturar(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
}

// ==========================================================
// PONTUAÇÃO (com ouvintes para atualizar as telas ao vivo)
// ==========================================================
class Pontuacao {
    private static int pontos = 0;
    private static int acertos = 0;
    private static int erros = 0;
    private static final List<Runnable> ouvintes = new ArrayList<>();
    private static final int[] LIMITES = {0, 30, 80, 150};
    private static final String[] NIVEIS = {"Iniciante", "Aprendiz", "Estudioso", "Mestre da Lógica"};

    public static void registrarResposta(boolean correta) {
        if (correta) {
            pontos += 10;
            acertos++;
        } else {
            erros++;
        }
        notificar();
    }

    public static void resetar() {
        pontos = 0;
        acertos = 0;
        erros = 0;
        notificar();
    }

    public static void addOuvinte(Runnable r) { ouvintes.add(r); }

    private static void notificar() {
        for (Runnable r : ouvintes) r.run();
    }

    public static int getPontos() { return pontos; }
    public static int getAcertos() { return acertos; }
    public static int getErros() { return erros; }
    public static int getTotal() { return acertos + erros; }

    public static double getPercentual() {
        if (getTotal() == 0) return 0;
        return (acertos * 100.0) / getTotal();
    }

    public static int indiceNivel() {
        int n = 0;
        for (int i = 0; i < LIMITES.length; i++) if (pontos >= LIMITES[i]) n = i;
        return n;
    }

    public static String getNivel() { return NIVEIS[indiceNivel()]; }
    public static int baseNivel() { return LIMITES[indiceNivel()]; }

    /** Pontos necessários para o próximo nível, ou -1 se já está no máximo. */
    public static int limiteProximoNivel() {
        int i = indiceNivel();
        return i + 1 < LIMITES.length ? LIMITES[i + 1] : -1;
    }
}

// ==========================================================
// UTILITÁRIOS DE INTERFACE
// ==========================================================
class UI {
    static final int LARGURA = 820;   // largura máxima dos cartões
    static final int TEXTO_L = 740;   // largura de texto dentro de um cartão

    static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    static RoundRectangle2D rr(double x, double y, double w, double h, double r) {
        return new RoundRectangle2D.Double(x, y, w, h, r, r);
    }

    static JLabel rotulo(String texto, Font fonte, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(fonte);
        l.setForeground(cor);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static Texto texto(String html, int largura) {
        return texto(html, largura, Cores.TEXTO);
    }

    static Texto texto(String html, int largura, Color cor) {
        return new Texto(html, Cores.TEXTO_NORMAL, cor, largura);
    }

    static Component espaco(int h) { return Box.createVerticalStrut(h); }

    static void centralizar(Graphics2D g2, String s, double cx, double cy) {
        FontMetrics fm = g2.getFontMetrics();
        double x = cx - fm.stringWidth(s) / 2.0;
        double y = cy + (fm.getAscent() - fm.getDescent()) / 2.0;
        g2.drawString(s, (float) x, (float) y);
    }

    static Cartao cartaoTexto(String titulo, Color cor, String html, int largura) {
        Cartao c = new Cartao(cor);
        c.add(rotulo(titulo, Cores.SUBTITULO, cor));
        c.add(espaco(10));
        c.add(texto(html, largura));
        return c;
    }
}

/** Painel transparente com largura máxima padrão (usado para linhas e grades). */
class Bloco extends JPanel {
    Bloco() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        iniciar();
    }

    Bloco(LayoutManager lm) {
        super(lm);
        iniciar();
    }

    private void iniciar() {
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(UI.LARGURA, getPreferredSize().height);
    }
}

// ==========================================================
// CARTÃO ARREDONDADO COM SOMBRA E FAIXA COLORIDA
// ==========================================================
class Cartao extends JPanel {
    private final Color destaque;
    private final int raio;
    private Color fundo = Cores.CARTAO;
    private int larguraMax = UI.LARGURA;

    Cartao(Color destaque) { this(destaque, 18); }

    Cartao(Color destaque, int raio) {
        this.destaque = destaque;
        this.raio = raio;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 28, 26, 24));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    void setFundo(Color c) { fundo = c; repaint(); }
    void setLarguraMax(int w) { larguraMax = w; }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(larguraMax, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth();
        int h = getHeight() - 6;
        for (int i = 1; i <= 5; i++) {
            g2.setColor(new Color(0, 0, 0, 24));
            g2.fill(UI.rr(0, i, w, h, raio));
        }
        g2.setColor(fundo);
        g2.fill(UI.rr(0, 0, w, h, raio));
        if (destaque != null) {
            Shape antigo = g2.getClip();
            g2.setClip(UI.rr(0, 0, w, h, raio));
            g2.setColor(destaque);
            g2.fillRect(0, 0, 6, h);
            g2.setClip(antigo);
        }
        g2.setColor(new Color(255, 255, 255, 12));
        g2.draw(UI.rr(0.5, 0.5, w - 1, h - 1, raio));
        g2.dispose();
        super.paintComponent(g);
    }
}

/**
 * Texto com quebra de linha automática e marcação simples (&lt;b&gt;, &lt;i&gt;, &lt;br&gt;, &lt;font color='#rrggbb'&gt;).
 * Mede e desenha com as mesmas métricas, então o texto nunca fica cortado,
 * não importa a fonte ou o sistema operacional do computador.
 */
class Marcado {
    static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private static class Palavra {
        final String s;
        final Font f;
        final Color c;
        final double w;

        Palavra(String s, Font f, Color c) {
            this.s = s;
            this.f = f;
            this.c = c;
            this.w = f.getStringBounds(s, FRC).getWidth();
        }
    }

    private static class Unidade {
        final List<Palavra> ps = new ArrayList<>();
        boolean espacoAntes, quebraAntes;
        double w, x;
    }

    private final Font base;
    private final Color corBase;
    private final List<Unidade> unidades = new ArrayList<>();
    private final List<List<Unidade>> linhas = new ArrayList<>();
    private int larguraLayout = -1;
    private final double larguraEspaco;
    private final int alturaLinha, ascent, deslocamento;

    Marcado(String markup, Font base, Color corBase) {
        this.base = base;
        this.corBase = corBase;
        this.larguraEspaco = base.getStringBounds(" ", FRC).getWidth();
        LineMetrics lm = base.getLineMetrics("Ágyp", FRC);
        int alt = (int) Math.ceil(lm.getAscent() + lm.getDescent());
        this.alturaLinha = (int) Math.round(alt * 1.25);
        this.deslocamento = (alturaLinha - alt) / 2;
        this.ascent = (int) Math.ceil(lm.getAscent());
        parse(markup);
    }

    void definir(String markup) {
        unidades.clear();
        linhas.clear();
        larguraLayout = -1;
        parse(markup);
    }

    private Font fonte(boolean b, boolean i) {
        int estilo = base.getStyle() | (b ? Font.BOLD : 0) | (i ? Font.ITALIC : 0);
        return base.deriveFont(estilo);
    }

    private void parse(String m) {
        if (m == null) m = "";
        boolean b = false, i = false;
        Color cor = corBase;
        java.util.ArrayDeque<Color> pilha = new java.util.ArrayDeque<>();
        StringBuilder w = new StringBuilder();
        boolean pendEsp = false, pendQuebra = false;
        int pos = 0;
        while (pos < m.length()) {
            char ch = m.charAt(pos);
            if (ch == '<') {
                int fim = m.indexOf('>', pos);
                if (fim < 0) fim = m.length() - 1;
                String tag = m.substring(pos + 1, fim).trim().toLowerCase();
                // fecha a palavra com o estilo atual antes de mudar de estilo
                if (w.length() > 0) {
                    fechar(w.toString(), fonte(b, i), cor, pendEsp, pendQuebra);
                    w.setLength(0);
                    pendEsp = false;
                    pendQuebra = false;
                }
                if (tag.equals("b")) b = true;
                else if (tag.equals("/b")) b = false;
                else if (tag.equals("i")) i = true;
                else if (tag.equals("/i")) i = false;
                else if (tag.startsWith("br")) pendQuebra = true;
                else if (tag.startsWith("font")) {
                    pilha.push(cor);
                    int h = tag.indexOf('#');
                    if (h >= 0 && tag.length() >= h + 7) cor = Color.decode(tag.substring(h, h + 7));
                } else if (tag.equals("/font")) {
                    if (!pilha.isEmpty()) cor = pilha.pop();
                }
                pos = fim + 1;
            } else if (m.startsWith("&nbsp;", pos)) {
                w.append(' ');
                pos += 6;
            } else if (ch == ' ' || ch == '\n' || ch == '\t') {
                if (w.length() > 0) {
                    fechar(w.toString(), fonte(b, i), cor, pendEsp, pendQuebra);
                    w.setLength(0);
                    pendEsp = false;
                    pendQuebra = false;
                }
                pendEsp = true;
                pos++;
            } else {
                w.append(ch);
                pos++;
            }
        }
        if (w.length() > 0) fechar(w.toString(), fonte(b, i), cor, pendEsp, pendQuebra);
    }

    private void fechar(String s, Font f, Color c, boolean espaco, boolean quebra) {
        Palavra p = new Palavra(s, f, c);
        if (unidades.isEmpty() || espaco || quebra) {
            Unidade u = new Unidade();
            u.espacoAntes = espaco;
            u.quebraAntes = quebra;
            u.ps.add(p);
            u.w = p.w;
            unidades.add(u);
        } else {
            Unidade u = unidades.get(unidades.size() - 1);
            u.ps.add(p);
            u.w += p.w;
        }
    }

    private void layout(int largura) {
        if (largura == larguraLayout) return;
        larguraLayout = largura;
        linhas.clear();
        List<Unidade> linha = new ArrayList<>();
        double x = 0;
        for (Unidade u : unidades) {
            double esp = (u.espacoAntes && !linha.isEmpty()) ? larguraEspaco : 0;
            boolean nova = !linha.isEmpty() && (u.quebraAntes || x + esp + u.w > largura);
            if (nova) {
                linhas.add(linha);
                linha = new ArrayList<>();
                x = 0;
                esp = 0;
            }
            u.x = x + esp;
            x = u.x + u.w;
            linha.add(u);
        }
        linhas.add(linha);
    }

    int altura(int largura) {
        layout(largura);
        return linhas.size() * alturaLinha;
    }

    void desenhar(Graphics2D g2, int x, int y, int largura) {
        layout(largura);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        for (int li = 0; li < linhas.size(); li++) {
            float baseline = y + li * alturaLinha + deslocamento + ascent;
            for (Unidade u : linhas.get(li)) {
                float px = x + (float) u.x;
                for (Palavra p : u.ps) {
                    g2.setFont(p.f);
                    g2.setColor(p.c);
                    g2.drawString(p.s, px, baseline);
                    px += (float) p.w;
                }
            }
        }
    }
}

class Texto extends JComponent {
    private final Marcado m;
    private final int largura;

    Texto(String markup, Font fonte, Color cor, int largura) {
        this.m = new Marcado(markup, fonte, cor);
        this.largura = largura;
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    void definir(String markup) {
        m.definir(markup);
        revalidate();
        repaint();
    }

    @Override public Dimension getPreferredSize() { return new Dimension(largura, m.altura(largura)); }
    @Override public Dimension getMinimumSize() { return getPreferredSize(); }
    @Override public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        m.desenhar(g2, 0, 0, largura);
        g2.dispose();
    }
}

/** Caixa de texto arredondada (exemplos, dicas, feedback). */
class Caixa extends JComponent {
    private Color fundo;
    private boolean ativo;
    private int largura;
    private final Marcado m;

    Caixa(String html, int largura, Color fundo) {
        this.fundo = fundo;
        this.largura = largura;
        this.ativo = html != null;
        this.m = new Marcado(html == null ? "" : html, Cores.TEXTO_NORMAL, Cores.TEXTO);
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /** Mantido por compatibilidade: o texto sempre começa no topo da caixa. */
    void setVerticalAlignment(int v) { }

    void mostrar(String html, int largura, Color fundo) {
        this.fundo = fundo;
        this.largura = largura;
        this.ativo = true;
        m.definir(html);
        revalidate();
        repaint();
    }

    void limpar() {
        ativo = false;
        m.definir("");
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();
        return new Dimension(largura + 28, m.altura(largura) + 20);
    }

    @Override
    public Dimension getMaximumSize() {
        return isMaximumSizeSet() ? super.getMaximumSize() : getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!ativo) return;
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        g2.setColor(fundo);
        g2.fill(UI.rr(0, 0, getWidth(), getHeight(), 12));
        m.desenhar(g2, 14, 10, largura);
        g2.dispose();
    }
}

// ==========================================================
// BOTÃO MODERNO COM ANIMAÇÃO DE HOVER
// ==========================================================
class Botao extends JButton {
    private Color cor;
    private float hover = 0f;
    private boolean alvoHover;
    private boolean toggle;
    private boolean selecionado;
    private final Timer anim;

    Botao(String texto, Color cor) {
        super(texto);
        this.cor = cor;
        setFont(new Font("Dialog", Font.BOLD, 16));
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(12, 26, 12, 26));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        anim = new Timer(16, e -> {
            float alvo = alvoHover ? 1f : 0f;
            hover += (alvo - hover) * 0.28f;
            if (Math.abs(alvo - hover) < 0.02f) {
                hover = alvo;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { alvoHover = true; anim.start(); }
            @Override public void mouseExited(MouseEvent e) { alvoHover = false; anim.start(); }
        });
    }

    void setCor(Color c) { cor = c; repaint(); }

    void setToggle(boolean t) {
        toggle = t;
        atualizarTexto();
        repaint();
    }

    void setSelecionado(boolean s) {
        selecionado = s;
        atualizarTexto();
        repaint();
    }

    private void atualizarTexto() {
        if (toggle) setForeground(selecionado ? Color.WHITE : Cores.TEXTO_SECUNDARIO);
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        Color base = cor;
        if (toggle && !selecionado) base = Cores.CARTAO_CLARO;
        float lum = 0.299f * base.getRed() + 0.587f * base.getGreen() + 0.114f * base.getBlue();
        Color alvo = lum > 200 ? new Color(150, 165, 255) : Color.WHITE;
        float k = (lum > 200 ? 0.35f : 0.16f) * hover;
        g2.setColor(Cores.misturar(base, alvo, k));
        g2.fill(UI.rr(0, 0, getWidth(), getHeight(), 14));
        g2.dispose();
        super.paintComponent(g);
    }
}

// ==========================================================
// BARRA DE PROGRESSO ANIMADA
// ==========================================================
class BarraProgresso extends JPanel {
    private double atual = 0, alvo = 0;
    private Color cor;

    BarraProgresso(Color cor) {
        this.cor = cor;
        setOpaque(false);
        setPreferredSize(new Dimension(200, 10));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    void setCor(Color c) { cor = c; repaint(); }

    void setValor(double v) {
        alvo = Math.max(0, Math.min(1, v));
        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            atual += (alvo - atual) * 0.15;
            if (Math.abs(alvo - atual) < 0.003) {
                atual = alvo;
                t.stop();
            }
            repaint();
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        g2.setColor(Cores.CARTAO_CLARO);
        g2.fill(UI.rr(0, 0, w, h, h));
        int fw = (int) Math.round(w * atual);
        if (fw > 0) {
            g2.setPaint(new GradientPaint(0, 0, cor, w, 0, Cores.misturar(cor, Color.WHITE, 0.35f)));
            g2.fill(UI.rr(0, 0, Math.max(fw, h), h, h));
        }
        g2.dispose();
    }
}

// ==========================================================
// OPÇÃO DE RESPOSTA (QUIZ)
// ==========================================================
class OpcaoResposta extends JButton {
    enum Estado { NORMAL, CORRETA, ERRADA, APAGADA }

    private Estado estado = Estado.NORMAL;
    private String letra = "A";
    private boolean hover;

    OpcaoResposta() {
        setFont(new Font("Dialog", Font.PLAIN, 17));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(760, 54));
        setMaximumSize(new Dimension(760, 54));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    void configurar(String letra, String texto) {
        this.letra = letra;
        setText(texto);
        estado = Estado.NORMAL;
        repaint();
    }

    void setEstado(Estado e) {
        estado = e;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        Color fundo, borda, texto = Cores.TEXTO, badge;
        boolean ativa = estado == Estado.NORMAL && hover;
        switch (estado) {
            case CORRETA:
                fundo = Cores.alfa(Cores.VERDE, 45); borda = Cores.VERDE; badge = Cores.VERDE; break;
            case ERRADA:
                fundo = Cores.alfa(Cores.VERMELHO, 45); borda = Cores.VERMELHO; badge = Cores.VERMELHO; break;
            case APAGADA:
                fundo = new Color(31, 35, 56); borda = new Color(42, 47, 72);
                texto = Cores.TEXTO_SECUNDARIO; badge = Cores.CARTAO_CLARO; break;
            default:
                fundo = ativa ? Cores.CARTAO_CLARO : new Color(35, 40, 63);
                borda = ativa ? Cores.AZUL : Cores.BORDA;
                badge = ativa ? Cores.AZUL : Cores.CARTAO_CLARO;
        }
        g2.setColor(fundo);
        g2.fill(UI.rr(0, 0, w, h, 14));
        g2.setColor(borda);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(UI.rr(1, 1, w - 2, h - 2, 14));

        g2.setColor(badge);
        g2.fill(new Ellipse2D.Double(14, (h - 30) / 2.0, 30, 30));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 15));
        UI.centralizar(g2, letra, 29, h / 2.0);

        g2.setColor(texto);
        g2.setFont(getFont());
        UI.centralizar(g2, "", 0, 0);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), 62, (h + fm.getAscent() - fm.getDescent()) / 2f);
        g2.dispose();
    }
}

// ==========================================================
// CÉLULAS DE TABELA
// ==========================================================
class Celula extends JLabel {
    private final Color fundo;

    Celula(String texto, Color fundo, Color letra) {
        super(texto, SwingConstants.CENTER);
        this.fundo = fundo;
        setFont(new Font("Dialog", Font.BOLD, 17));
        setForeground(letra);
        setOpaque(false);
        setPreferredSize(new Dimension(76, 38));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        g2.setColor(fundo);
        g2.fill(UI.rr(0, 0, getWidth(), getHeight(), 10));
        g2.dispose();
        super.paintComponent(g);
    }
}

/** Célula clicável do desafio: alterna entre ?, V e F. */
class CelulaTabela extends JButton {
    int estado = 0;        // 0 = ?, 1 = V, 2 = F
    int verificado = 0;    // 0 = nada, 1 = certa, 2 = errada

    CelulaTabela() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(new Font("Dialog", Font.BOLD, 19));
        setPreferredSize(new Dimension(90, 42));
        addActionListener(e -> {
            estado = estado == 0 ? 1 : (estado == 1 ? 2 : 1);
            verificado = 0;
            repaint();
        });
    }

    void reset() {
        estado = 0;
        verificado = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        Color fundo = Cores.CARTAO_CLARO, letra = Cores.TEXTO_SECUNDARIO;
        String t = "?";
        if (estado == 1) { fundo = Cores.alfa(Cores.VERDE, 60); letra = Cores.VERDE; t = "V"; }
        if (estado == 2) { fundo = Cores.alfa(Cores.VERMELHO, 60); letra = Cores.VERMELHO; t = "F"; }
        g2.setColor(fundo);
        g2.fill(UI.rr(0, 0, w, h, 10));
        if (verificado != 0) {
            g2.setColor(verificado == 1 ? Cores.VERDE : Cores.VERMELHO);
            g2.setStroke(new BasicStroke(3f));
            g2.draw(UI.rr(1.5, 1.5, w - 3, h - 3, 10));
        } else {
            g2.setColor(Cores.alfa(Cores.AMARELO, estado == 0 ? 120 : 0));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 4f}, 0f));
            g2.draw(UI.rr(1, 1, w - 2, h - 2, 10));
        }
        g2.setColor(letra);
        g2.setFont(getFont());
        UI.centralizar(g2, t, w / 2.0, h / 2.0);
        g2.dispose();
    }
}

// ==========================================================
// ROLAGEM
// ==========================================================
class PainelRolavel extends JPanel implements Scrollable {
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle v, int o, int d) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle v, int o, int d) { return Math.max(40, v.height - 60); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

class BarraRolagemFina extends BasicScrollBarUI {
    @Override
    protected void configureScrollBarColors() {
        thumbColor = Cores.BORDA;
        trackColor = Cores.FUNDO;
    }

    private JButton vazio() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        return b;
    }

    @Override protected JButton createDecreaseButton(int orientation) { return vazio(); }
    @Override protected JButton createIncreaseButton(int orientation) { return vazio(); }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(Cores.FUNDO);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        g2.setColor(thumbColor);
        g2.fill(UI.rr(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8));
        g2.dispose();
    }
}

/** Base de todas as telas: cabeçalho + conteúdo centralizado e rolável. */
abstract class Pagina extends JPanel {
    protected final JPanel corpo;

    Pagina(String etiqueta, String titulo, String subtitulo, Color corEtiqueta) {
        setLayout(new BorderLayout());
        setBackground(Cores.FUNDO);

        corpo = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 900;
                return d;
            }
        };
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        corpo.setBackground(Cores.FUNDO);
        corpo.setBorder(new EmptyBorder(34, 40, 40, 40));

        if (titulo != null) {
            if (etiqueta != null) {
                corpo.add(UI.rotulo(etiqueta.toUpperCase(), Cores.ETIQUETA, corEtiqueta));
                corpo.add(UI.espaco(6));
            }
            corpo.add(UI.rotulo(titulo, Cores.TITULO, Cores.TEXTO));
            if (subtitulo != null) {
                corpo.add(UI.espaco(8));
                corpo.add(UI.texto(subtitulo, 780, Cores.TEXTO_SECUNDARIO));
            }
            corpo.add(UI.espaco(26));
        }

        PainelRolavel wrapper = new PainelRolavel();
        wrapper.setLayout(new GridBagLayout());
        wrapper.setBackground(Cores.FUNDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        wrapper.add(corpo, gbc);

        JScrollPane sp = new JScrollPane(wrapper,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getViewport().setBackground(Cores.FUNDO);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getVerticalScrollBar().setUI(new BarraRolagemFina());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        add(sp, BorderLayout.CENTER);
    }

    protected void adicionar(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        corpo.add(c);
        corpo.add(UI.espaco(18));
    }

    protected void secao(String titulo) {
        corpo.add(UI.rotulo(titulo, Cores.SUBTITULO, Cores.TEXTO));
        corpo.add(UI.espaco(12));
    }
}

// ==========================================================
// DIAGRAMAS DE VENN
// ==========================================================
class VennPanel extends JPanel {
    enum Op { NENHUMA, UNIAO, INTERSECCAO, A_MENOS_B, B_MENOS_A }

    private Set<Integer> a = new TreeSet<>();
    private Set<Integer> b = new TreeSet<>();
    private Op op = Op.NENHUMA;
    private final boolean animado;
    private final boolean elementos;
    private double fase = 0;
    private Color corA = Cores.AZUL, corB = Cores.ROXO, corRes = Cores.CIANO;
    private final Timer timer;

    VennPanel(boolean animado, boolean elementos, int w, int h) {
        this.animado = animado;
        this.elementos = elementos;
        setOpaque(false);
        setPreferredSize(new Dimension(w, h));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        timer = new Timer(33, e -> {
            fase += 0.06;
            if (isShowing()) repaint();
        });
    }

    @Override public void addNotify() { super.addNotify(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    void setCores(Color a, Color b, Color res) { corA = a; corB = b; corRes = res; }
    void setOp(Op o) { op = o; repaint(); }

    void setDados(Set<Integer> a, Set<Integer> b) {
        this.a = new TreeSet<>(a);
        this.b = new TreeSet<>(b);
        repaint();
    }

    static Set<Integer> calcular(Op op, Set<Integer> a, Set<Integer> b) {
        Set<Integer> r = new TreeSet<>();
        switch (op) {
            case UNIAO: r.addAll(a); r.addAll(b); break;
            case INTERSECCAO: r.addAll(a); r.retainAll(b); break;
            case A_MENOS_B: r.addAll(a); r.removeAll(b); break;
            case B_MENOS_A: r.addAll(b); r.removeAll(a); break;
            default: break;
        }
        return r;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();

        if (elementos) {
            g2.setColor(Cores.BORDA);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 5f}, 0f));
            g2.draw(UI.rr(4, 4, w - 8, h - 8, 18));
            g2.setFont(new Font("Dialog", Font.BOLD, 18));
            g2.setColor(Cores.TEXTO_SECUNDARIO);
            g2.drawString("U", 20, 30);
        }

        double r = Math.min(h * 0.34, w * 0.20);
        double d = animado ? r * (1.15 + 0.35 * Math.sin(fase)) : r * 1.1;
        double cx = w / 2.0;
        double cy = h / 2.0 + (elementos ? 10 : 0);
        double xa = cx - d / 2, xb = cx + d / 2;

        Area areaA = new Area(new Ellipse2D.Double(xa - r, cy - r, 2 * r, 2 * r));
        Area areaB = new Area(new Ellipse2D.Double(xb - r, cy - r, 2 * r, 2 * r));
        Area res = new Area();
        switch (op) {
            case UNIAO: res = new Area(areaA); res.add(areaB); break;
            case INTERSECCAO: res = new Area(areaA); res.intersect(areaB); break;
            case A_MENOS_B: res = new Area(areaA); res.subtract(areaB); break;
            case B_MENOS_A: res = new Area(areaB); res.subtract(areaA); break;
            default:
                if (animado) { res = new Area(areaA); res.intersect(areaB); }
        }

        g2.setColor(Cores.alfa(corA, 50));
        g2.fill(areaA);
        g2.setColor(Cores.alfa(corB, 50));
        g2.fill(areaB);
        int pulso = (int) (125 + 50 * Math.sin(fase * 1.6));
        g2.setColor(Cores.alfa(corRes, pulso));
        g2.fill(res);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(corA);
        g2.draw(areaA);
        g2.setColor(corB);
        g2.draw(areaB);

        g2.setFont(new Font("Dialog", Font.BOLD, 24));
        g2.setColor(corA);
        UI.centralizar(g2, "A", xa - r * 0.62, cy - r - 14);
        g2.setColor(corB);
        UI.centralizar(g2, "B", xb + r * 0.62, cy - r - 14);

        if (elementos) {
            List<Integer> soA = new ArrayList<>(a);
            soA.removeAll(b);
            List<Integer> inter = new ArrayList<>(a);
            inter.retainAll(b);
            List<Integer> soB = new ArrayList<>(b);
            soB.removeAll(a);
            Set<Integer> dest = calcular(op, a, b);
            desenharElementos(g2, soA, xa - r + d / 2, cy, dest);
            desenharElementos(g2, inter, cx, cy, dest);
            desenharElementos(g2, soB, xb + r - d / 2, cy, dest);
        }
        g2.dispose();
    }

    private void desenharElementos(Graphics2D g2, List<Integer> els, double cx, double cy, Set<Integer> dest) {
        int n = els.size();
        if (n == 0) return;
        int cols = n > 3 ? 2 : 1;
        int rows = (n + cols - 1) / cols;
        g2.setFont(new Font("Dialog", Font.BOLD, 18));
        for (int i = 0; i < n; i++) {
            int col = i % cols, row = i / cols;
            double x = cx + (col - (cols - 1) / 2.0) * 36;
            double y = cy + (row - (rows - 1) / 2.0) * 36;
            boolean on = dest.contains(els.get(i));
            if (on) {
                g2.setColor(Cores.CIANO);
                g2.fill(new Ellipse2D.Double(x - 15, y - 15, 30, 30));
                g2.setColor(Cores.FUNDO);
            } else {
                g2.setColor(Cores.TEXTO_SECUNDARIO);
            }
            UI.centralizar(g2, String.valueOf(els.get(i)), x, y);
        }
    }
}

/** Desenhos pequenos para a teoria (conjunto simples e subconjunto). */
class DesenhoConjunto extends JPanel {
    static final int SIMPLES = 0, SUB = 1;
    private final int tipo;

    DesenhoConjunto(int tipo, int w, int h) {
        this.tipo = tipo;
        setOpaque(false);
        setPreferredSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        g2.setStroke(new BasicStroke(3f));
        g2.setFont(new Font("Dialog", Font.BOLD, 20));
        if (tipo == SIMPLES) {
            Ellipse2D e = new Ellipse2D.Double(w * 0.06, h * 0.08, w * 0.88, h * 0.84);
            g2.setColor(Cores.alfa(Cores.AZUL, 60));
            g2.fill(e);
            g2.setColor(Cores.AZUL);
            g2.draw(e);
            g2.setFont(new Font("Dialog", Font.BOLD, 22));
            UI.centralizar(g2, "A", w * 0.22, h * 0.24);
            g2.setColor(Cores.TEXTO);
            g2.setFont(new Font("Dialog", Font.BOLD, 20));
            UI.centralizar(g2, "2", w * 0.40, h * 0.40);
            UI.centralizar(g2, "4", w * 0.60, h * 0.40);
            UI.centralizar(g2, "6", w * 0.40, h * 0.64);
            UI.centralizar(g2, "8", w * 0.60, h * 0.64);
        } else {
            Ellipse2D b = new Ellipse2D.Double(4, 6, w - 8, h - 12);
            g2.setColor(Cores.alfa(Cores.ROXO, 50));
            g2.fill(b);
            g2.setColor(Cores.ROXO);
            g2.draw(b);
            Ellipse2D a = new Ellipse2D.Double(w * 0.12, h * 0.27, w * 0.42, h * 0.46);
            g2.setColor(Cores.alfa(Cores.AZUL, 90));
            g2.fill(a);
            g2.setColor(Cores.AZUL);
            g2.draw(a);
            g2.setFont(new Font("Dialog", Font.BOLD, 22));
            g2.setColor(Cores.ROXO);
            UI.centralizar(g2, "B", w * 0.76, h * 0.20);
            g2.setColor(Cores.AZUL);
            UI.centralizar(g2, "A", w * 0.33, h * 0.20);
            g2.setColor(Cores.TEXTO);
            g2.setFont(new Font("Dialog", Font.BOLD, 20));
            UI.centralizar(g2, "1", w * 0.26, h * 0.50);
            UI.centralizar(g2, "2", w * 0.42, h * 0.50);
            UI.centralizar(g2, "3", w * 0.72, h * 0.46);
            UI.centralizar(g2, "4", w * 0.72, h * 0.68);
        }
        g2.dispose();
    }
}

// ==========================================================
// TELA PRINCIPAL (JANELA) + MENU LATERAL
// ==========================================================
class TelaInicial extends JFrame {
    private final CardLayout cl = new CardLayout();
    private final JPanel conteudo = new JPanel(cl);
    private final MenuLateral menu;
    private final TelaAtividades atividades;

    public TelaInicial() {
        setTitle("LogicApp - Conjuntos e Lógica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension tela = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1320, tela.width - 40), Math.min(860, tela.height - 70));
        setMinimumSize(new Dimension(1120, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Cores.FUNDO);
        setLayout(new BorderLayout());

        atividades = new TelaAtividades();
        menu = new MenuLateral(this::ir);

        conteudo.setBackground(Cores.FUNDO);
        conteudo.add(new TelaInicio(this::ir), "inicio");
        conteudo.add(new TelaTeoria(this::ir), "conjuntos");
        conteudo.add(new TelaOperacoes(this::ir), "operacoes");
        conteudo.add(new TelaTabelasVerdade(this::ir), "tabelas");
        conteudo.add(atividades, "atividades");
        conteudo.add(new TelaProva(), "prova");
        conteudo.add(new TelaDesempenho(), "desempenho");

        add(menu, BorderLayout.WEST);
        add(conteudo, BorderLayout.CENTER);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F11"), "tela-cheia");
        getRootPane().getActionMap().put("tela-cheia", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setExtendedState(getExtendedState() == JFrame.MAXIMIZED_BOTH
                        ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
            }
        });

        ir("inicio");
    }

    /** Aceita "tela" ou "atividades:2" (abre a atividade do índice indicado). */
    void ir(String destino) {
        String[] partes = destino.split(":");
        String tela = partes[0];
        if (tela.equals("atividades") && partes.length > 1) {
            atividades.selecionar(Integer.parseInt(partes[1]));
        }
        cl.show(conteudo, tela);
        menu.selecionar(tela);
    }
}

class Logo extends JComponent {
    Logo() {
        setPreferredSize(new Dimension(236, 46));
        setMaximumSize(new Dimension(236, 46));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        Area c1 = new Area(new Ellipse2D.Double(2, 8, 30, 30));
        Area c2 = new Area(new Ellipse2D.Double(18, 8, 30, 30));
        Area inter = new Area(c1);
        inter.intersect(c2);
        g2.setColor(Cores.alfa(Cores.AZUL, 190));
        g2.fill(c1);
        g2.setColor(Cores.alfa(Cores.ROXO, 190));
        g2.fill(c2);
        g2.setColor(Cores.CIANO);
        g2.fill(inter);
        g2.setFont(new Font("Dialog", Font.BOLD, 27));
        g2.setColor(Color.WHITE);
        g2.drawString("Logic", 60, 33);
        int w = g2.getFontMetrics().stringWidth("Logic");
        g2.setColor(Cores.AZUL);
        g2.drawString("App", 60 + w, 33);
        g2.dispose();
    }
}

class ItemMenu extends JButton {
    final String id;
    private final String simbolo;
    private final String nome;
    private final Color cor;
    private boolean selecionado;
    private boolean hover;

    ItemMenu(String id, String simbolo, String nome, Color cor) {
        super(nome);
        this.id = id;
        this.simbolo = simbolo;
        this.nome = nome;
        this.cor = cor;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(236, 50));
        setMaximumSize(new Dimension(236, 50));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    void setSelecionado(boolean s) {
        selecionado = s;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        if (selecionado) {
            g2.setColor(Cores.alfa(cor, 40));
            g2.fill(UI.rr(0, 0, w, h, 14));
            g2.setColor(cor);
            g2.fill(UI.rr(0, 11, 4, h - 22, 4));
        } else if (hover) {
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fill(UI.rr(0, 0, w, h, 14));
        }
        int b = 32, bx = 14, by = (h - b) / 2;
        g2.setColor(selecionado ? cor : Cores.alfa(cor, 55));
        g2.fill(UI.rr(bx, by, b, b, 10));
        g2.setColor(selecionado ? Color.WHITE : cor);
        g2.setFont(new Font("Dialog", Font.BOLD, 17));
        UI.centralizar(g2, simbolo, bx + b / 2.0, h / 2.0);

        g2.setColor(selecionado ? Cores.TEXTO : (hover ? Cores.TEXTO : Cores.TEXTO_SECUNDARIO));
        g2.setFont(new Font("Dialog", selecionado ? Font.BOLD : Font.PLAIN, 16));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(nome, 60, (h + fm.getAscent() - fm.getDescent()) / 2f);
        g2.dispose();
    }
}

class MenuLateral extends JPanel {
    private final List<ItemMenu> itens = new ArrayList<>();
    private final JLabel lblPontos = new JLabel("0");
    private final JLabel lblNivel = new JLabel("Iniciante");

    MenuLateral(Consumer<String> nav) {
        setPreferredSize(new Dimension(272, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(30, 18, 22, 18));

        add(new Logo());
        add(UI.espaco(2));
        add(UI.rotulo("Conjuntos & Lógica", Cores.TEXTO_PEQUENO, Cores.TEXTO_SECUNDARIO));
        add(UI.espaco(34));
        add(UI.rotulo("  MENU", Cores.ETIQUETA, Cores.BORDA));
        add(UI.espaco(8));

        adicionar(nav, "inicio", "\u25CF", "Início", Cores.AZUL);
        adicionar(nav, "conjuntos", "\u2208", "Teoria dos Conjuntos", Cores.AZUL);
        adicionar(nav, "operacoes", "\u222A", "Operações", Cores.ROXO);
        adicionar(nav, "tabelas", "\u2227", "Tabelas-Verdade", Cores.CIANO);
        adicionar(nav, "atividades", "?", "Atividades", Cores.AMARELO);
        adicionar(nav, "prova", "\u2713", "Simulado (Prova)", Cores.VERDE);
        adicionar(nav, "desempenho", "%", "Meu Desempenho", Cores.LARANJA);

        add(Box.createVerticalGlue());

        Cartao placar = new Cartao(Cores.AMARELO, 14);
        placar.setFundo(Cores.CARTAO_CLARO);
        placar.setLarguraMax(236);
        placar.setBorder(new EmptyBorder(12, 20, 18, 12));
        placar.add(UI.rotulo("SEUS PONTOS", Cores.ETIQUETA, Cores.TEXTO_SECUNDARIO));
        lblPontos.setFont(new Font("Dialog", Font.BOLD, 30));
        lblPontos.setForeground(Cores.AMARELO);
        lblPontos.setAlignmentX(Component.LEFT_ALIGNMENT);
        placar.add(lblPontos);
        lblNivel.setFont(Cores.TEXTO_PEQUENO);
        lblNivel.setForeground(Cores.TEXTO);
        lblNivel.setAlignmentX(Component.LEFT_ALIGNMENT);
        placar.add(lblNivel);
        add(placar);
        add(UI.espaco(10));
        add(UI.rotulo("  F11  \u00B7  tela cheia", Cores.TEXTO_PEQUENO, Cores.BORDA));

        Pontuacao.addOuvinte(this::atualizarPlacar);
        atualizarPlacar();
    }

    private void atualizarPlacar() {
        lblPontos.setText(Pontuacao.getPontos() + " pts");
        lblNivel.setText("Nível: " + Pontuacao.getNivel());
    }

    private void adicionar(Consumer<String> nav, String id, String simbolo, String nome, Color cor) {
        ItemMenu item = new ItemMenu(id, simbolo, nome, cor);
        item.addActionListener(e -> nav.accept(id));
        itens.add(item);
        add(item);
        add(UI.espaco(4));
    }

    void selecionar(String id) {
        for (ItemMenu i : itens) i.setSelecionado(i.id.equals(id));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, Cores.MENU_TOPO, 0, getHeight(), Cores.MENU_BASE));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 255, 255, 14));
        g2.fillRect(getWidth() - 1, 0, 1, getHeight());
        g2.dispose();
    }
}

// ==========================================================
// TELA: INÍCIO
// ==========================================================
class Heroi extends JPanel {
    Heroi(Consumer<String> nav) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(34, 40, 34, 24));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        Bloco esq = new Bloco();
        esq.add(UI.rotulo("MATEMÁTICA  \u2022  ENSINO MÉDIO", Cores.ETIQUETA, new Color(215, 224, 255)));
        esq.add(UI.espaco(10));
        esq.add(new Texto("Aprenda Lógica<br>e Conjuntos", Cores.TITULO_GRANDE, Color.WHITE, 420));
        esq.add(UI.espaco(14));
        esq.add(UI.texto("Descubra como a matemática organiza o mundo, de playlists a programas de computador.",
                370, new Color(228, 233, 255)));
        esq.add(UI.espaco(24));
        Botao comecar = new Botao("Começar a aprender  \u2192", Color.WHITE);
        comecar.setForeground(Cores.FUNDO);
        comecar.addActionListener(e -> nav.accept("conjuntos"));
        esq.add(comecar);

        VennPanel venn = new VennPanel(true, false, 330, 240);
        venn.setCores(Color.WHITE, new Color(255, 225, 150), Color.WHITE);

        add(esq, BorderLayout.CENTER);
        add(venn, BorderLayout.EAST);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(UI.LARGURA, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        Shape forma = UI.rr(0, 0, w, h, 28);
        g2.setPaint(new GradientPaint(0, 0, new Color(58, 82, 225), w, h, new Color(132, 72, 232)));
        g2.fill(forma);
        Shape antigo = g2.getClip();
        g2.setClip(forma);
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fill(new Ellipse2D.Double(w - 190, -110, 330, 330));
        g2.setColor(new Color(255, 255, 255, 12));
        g2.fill(new Ellipse2D.Double(-70, h - 110, 250, 250));
        g2.setClip(antigo);
        g2.dispose();
        super.paintComponent(g);
    }
}

class TelaInicio extends Pagina {
    TelaInicio(Consumer<String> nav) {
        super(null, null, null, null);
        adicionar(new Heroi(nav));

        secao("Seu caminho de estudo");
        Bloco passos = new Bloco(new GridLayout(1, 4, 14, 0));
        passos.add(passo("1", "Conjuntos", "O que são, como escrever e representar.", Cores.AZUL, "conjuntos", nav));
        passos.add(passo("2", "Operações", "União, interseção e diferença ao vivo.", Cores.ROXO, "operacoes", nav));
        passos.add(passo("3", "Lógica", "Quando uma frase é verdadeira ou falsa.", Cores.CIANO, "tabelas", nav));
        passos.add(passo("4", "Pratique", "Atividades, desafios e simulado.", Cores.AMARELO, "atividades", nav));
        adicionar(passos);

        secao("A lógica está em todo lugar");
        Bloco grade = new Bloco(new GridLayout(2, 2, 16, 16));
        grade.add(UI.cartaoTexto("Programação", Cores.AZUL,
                "Todo <b>if</b> de um programa usa lógica: <i>se (idade ≥ 18 E tem documento) então entrar</i>.", 330));
        grade.add(UI.cartaoTexto("Buscas na internet", Cores.ROXO,
                "Buscadores combinam palavras com <b>E</b> e <b>OU</b>. É a mesma ideia de interseção e união.", 330));
        grade.add(UI.cartaoTexto("Circuitos eletrônicos", Cores.CIANO,
                "As portas lógicas <b>AND, OR e NOT</b> dos chips do celular são as tabelas-verdade que você vai ver.", 330));
        grade.add(UI.cartaoTexto("Redes sociais", Cores.VERDE,
                "Os <b>amigos em comum</b> entre duas pessoas formam a <b>interseção</b> dos conjuntos de amigos.", 330));
        adicionar(grade);

        Cartao curiosidade = UI.cartaoTexto("Você sabia?", Cores.AMARELO,
                "A teoria dos conjuntos foi criada pelo matemático alemão <b>Georg Cantor</b>, no final do século XIX. "
                        + "Os diagramas que usamos para desenhar conjuntos levam o nome do inglês <b>John Venn</b>, "
                        + "que os popularizou em 1880.", UI.TEXTO_L);
        adicionar(curiosidade);
    }

    private Cartao passo(String numero, String titulo, String desc, Color cor, String destino, Consumer<String> nav) {
        Cartao c = new Cartao(cor, 16);
        c.setBorder(new EmptyBorder(18, 22, 24, 14));
        c.add(UI.rotulo(numero, new Font("Dialog", Font.BOLD, 34), cor));
        c.add(UI.rotulo(titulo, new Font("Dialog", Font.BOLD, 18), Cores.TEXTO));
        c.add(UI.espaco(6));
        c.add(UI.texto(desc, 140, Cores.TEXTO_SECUNDARIO));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { nav.accept(destino); }
        });
        return c;
    }
}

// ==========================================================
// TELA: TEORIA DOS CONJUNTOS (MÓDULO 1)
// ==========================================================
class TelaTeoria extends Pagina {
    private static final Set<Integer> CONJUNTO_A = new TreeSet<>(Arrays.asList(2, 4, 6, 8));

    TelaTeoria(Consumer<String> nav) {
        super("Módulo 1", "Teoria dos Conjuntos",
                "Conjuntos são a base da matemática moderna. Vamos entender o que são, como representar e como relacioná-los.",
                Cores.AZUL);

        adicionar(UI.cartaoTexto("O que é um conjunto?", Cores.AZUL,
                "Um <b>conjunto</b> é uma coleção de elementos que compartilham uma característica. "
                        + "Damos nome a ele com uma <b>letra maiúscula</b> e escrevemos os elementos entre <b>chaves</b>.<br><br>"
                        + "<b>Exemplo:</b> A = {1, 2, 3, 4, 5}<br><br>"
                        + "Dois detalhes: a <b>ordem</b> não importa ({1, 2, 3} = {3, 2, 1}) e <b>repetir</b> um elemento "
                        + "não muda nada ({1, 1, 2} = {1, 2}).<br><br>"
                        + "<b>No dia a dia:</b> as vogais, os dias do fim de semana, os jogadores de um time.",
                UI.TEXTO_L));

        adicionar(cartaoRepresentacao());
        adicionar(cartaoPertinencia());
        adicionar(cartaoSubconjunto());
        adicionar(cartaoEspeciais());

        adicionar(UI.cartaoTexto("Quantos elementos? Cardinalidade", Cores.AMARELO,
                "O número de elementos de um conjunto é a sua <b>cardinalidade</b>, escrita <b>n(A)</b>.<br><br>"
                        + "• A = {a, b, c} → <b>n(A) = 3</b><br>"
                        + "• B = {10, 20} → <b>n(B) = 2</b><br>"
                        + "• Conjunto vazio → <b>n(∅) = 0</b>", UI.TEXTO_L));

        Botao praticar = new Botao("Praticar Teoria dos Conjuntos  \u2192", Cores.AZUL);
        praticar.addActionListener(e -> nav.accept("atividades:0"));
        adicionar(praticar);
    }

    private Cartao cartaoRepresentacao() {
        Cartao c = new Cartao(Cores.ROXO);
        c.add(UI.rotulo("Três formas de representar", Cores.SUBTITULO, Cores.ROXO));
        c.add(UI.espaco(12));
        Bloco linha = new Bloco(new BorderLayout(20, 0));
        Bloco textos = new Bloco();
        textos.add(new Caixa("<b>1. Listagem</b> — escrevemos todos os elementos:<br>A = {2, 4, 6, 8}", 440, Cores.CARTAO_CLARO));
        textos.add(UI.espaco(10));
        textos.add(new Caixa("<b>2. Propriedade</b> — damos a regra:<br>A = {x | x é par e 2 ≤ x ≤ 8}", 440, Cores.CARTAO_CLARO));
        textos.add(UI.espaco(10));
        textos.add(new Caixa("<b>3. Diagrama de Venn</b> — uma curva fechada com os elementos dentro (ao lado).", 440, Cores.CARTAO_CLARO));
        linha.add(textos, BorderLayout.CENTER);
        linha.add(new DesenhoConjunto(DesenhoConjunto.SIMPLES, 240, 170), BorderLayout.EAST);
        c.add(linha);
        return c;
    }

    private Cartao cartaoPertinencia() {
        Cartao c = new Cartao(Cores.CIANO);
        c.add(UI.rotulo("Pertinência: ∈ e ∉", Cores.SUBTITULO, Cores.CIANO));
        c.add(UI.espaco(10));
        c.add(UI.texto("A pertinência liga um <b>elemento</b> a um <b>conjunto</b>.<br>"
                + "<b>∈</b> significa <i>pertence</i> e <b>∉</b> significa <i>não pertence</i>.", UI.TEXTO_L));
        c.add(UI.espaco(14));
        c.add(UI.rotulo("Experimente! Considere A = {2, 4, 6, 8} e clique em um número:", Cores.TEXTO_PEQUENO, Cores.TEXTO_SECUNDARIO));
        c.add(UI.espaco(8));

        Caixa resposta = new Caixa("Escolha um número acima para testar.", 700, Cores.CARTAO_CLARO);
        resposta.setPreferredSize(new Dimension(740, 48));
        resposta.setMaximumSize(new Dimension(740, 48));

        Bloco chips = new Bloco(new FlowLayout(FlowLayout.LEFT, 8, 0));
        List<Botao> lista = new ArrayList<>();
        for (int n = 1; n <= 10; n++) {
            final int numero = n;
            Botao b = new Botao(String.valueOf(n), Cores.CIANO);
            b.setToggle(true);
            b.setBorder(new EmptyBorder(8, 0, 8, 0));
            b.setPreferredSize(new Dimension(52, 42));
            lista.add(b);
            b.addActionListener(e -> {
                for (Botao o : lista) o.setSelecionado(o == b);
                if (CONJUNTO_A.contains(numero)) {
                    resposta.mostrar("<b>" + numero + " ∈ A</b> — o " + numero + " <font color='" + UI.hex(Cores.VERDE)
                            + "'><b>pertence</b></font> ao conjunto A.", 700, Cores.alfa(Cores.VERDE, 40));
                } else {
                    resposta.mostrar("<b>" + numero + " ∉ A</b> — o " + numero + " <font color='" + UI.hex(Cores.VERMELHO)
                            + "'><b>não pertence</b></font> ao conjunto A.", 700, Cores.alfa(Cores.VERMELHO, 40));
                }
            });
            chips.add(b);
        }
        c.add(chips);
        c.add(UI.espaco(12));
        c.add(resposta);
        return c;
    }

    private Cartao cartaoSubconjunto() {
        Cartao c = new Cartao(Cores.VERDE);
        c.add(UI.rotulo("Subconjuntos: ⊂", Cores.SUBTITULO, Cores.VERDE));
        c.add(UI.espaco(10));
        Bloco linha = new Bloco(new BorderLayout(20, 0));
        linha.add(UI.texto("Dizemos que <b>A ⊂ B</b> (A está contido em B) quando <b>todo elemento de A também é elemento de B</b>.<br><br>"
                + "<b>Exemplo:</b> A = {1, 2} e B = {1, 2, 3, 4} → A ⊂ B.<br><br>"
                + "<b>Cuidado:</b> ∈ liga elemento a conjunto; ⊂ liga conjunto a conjunto.<br><br>"
                + "Todo conjunto é subconjunto de si mesmo, e o conjunto vazio é subconjunto de qualquer conjunto.", 470),
                BorderLayout.CENTER);
        linha.add(new DesenhoConjunto(DesenhoConjunto.SUB, 240, 180), BorderLayout.EAST);
        c.add(linha);
        return c;
    }

    private Cartao cartaoEspeciais() {
        Cartao c = new Cartao(Cores.LARANJA);
        c.add(UI.rotulo("Conjuntos especiais", Cores.SUBTITULO, Cores.LARANJA));
        c.add(UI.espaco(12));
        Bloco grade = new Bloco(new GridLayout(2, 2, 14, 14));
        grade.add(mini("∅", "Conjunto vazio", "Não tem nenhum elemento. Escrevemos ∅ ou { }.", Cores.AZUL));
        grade.add(mini("U", "Conjunto universo", "Reúne todos os elementos do assunto em estudo.", Cores.ROXO));
        grade.add(mini("1", "Conjunto unitário", "Tem um único elemento. Exemplo: {5}.", Cores.CIANO));
        grade.add(mini("∞", "Conjunto infinito", "Nunca termina. Exemplo: N = {0, 1, 2, 3, ...}.", Cores.VERDE));
        c.add(grade);
        return c;
    }

    private Cartao mini(String simbolo, String nome, String desc, Color cor) {
        Cartao c = new Cartao(null, 14);
        c.setFundo(Cores.CARTAO_CLARO);
        c.setBorder(new EmptyBorder(14, 18, 20, 14));
        c.add(UI.rotulo(simbolo, new Font("Dialog", Font.BOLD, 34), cor));
        c.add(UI.rotulo(nome, new Font("Dialog", Font.BOLD, 18), Cores.TEXTO));
        c.add(UI.espaco(4));
        c.add(UI.texto(desc, 320, Cores.TEXTO_SECUNDARIO));
        return c;
    }
}

// ==========================================================
// TELA: OPERAÇÕES (MÓDULO 2) + LABORATÓRIO DE VENN
// ==========================================================
class LaboratorioConjuntos extends Cartao {
    private final Set<Integer> a = new TreeSet<>();
    private final Set<Integer> b = new TreeSet<>();
    private final VennPanel venn = new VennPanel(false, true, 768, 310);
    private final Texto lblConjuntos = new Texto("", new Font("Dialog", Font.BOLD, 19), Cores.TEXTO, 740);
    private final Caixa definicao = new Caixa("", 730, Cores.CARTAO_CLARO);
    private final Caixa resultado = new Caixa("", 730, Cores.alfa(Cores.CIANO, 40));
    private final List<Botao> botoes = new ArrayList<>();
    private VennPanel.Op atual = VennPanel.Op.UNIAO;
    private final Random rnd = new Random();

    LaboratorioConjuntos() {
        super(Cores.CIANO);
        add(UI.rotulo("Laboratório de Conjuntos", Cores.SUBTITULO, Cores.CIANO));
        add(UI.espaco(4));
        add(UI.texto("Escolha uma operação e veja no diagrama quais elementos entram no resultado (destacados em azul-claro).",
                UI.TEXTO_L, Cores.TEXTO_SECUNDARIO));
        add(UI.espaco(14));

        add(lblConjuntos);
        add(UI.espaco(12));

        Bloco linha = new Bloco(new FlowLayout(FlowLayout.LEFT, 10, 0));
        adicionarOp(linha, "A ∪ B", VennPanel.Op.UNIAO);
        adicionarOp(linha, "A ∩ B", VennPanel.Op.INTERSECCAO);
        adicionarOp(linha, "A − B", VennPanel.Op.A_MENOS_B);
        adicionarOp(linha, "B − A", VennPanel.Op.B_MENOS_A);
        Botao sortear = new Botao("Sortear novos conjuntos", Cores.ROXO);
        sortear.addActionListener(e -> {
            sortear();
            atualizar();
        });
        linha.add(sortear);
        add(linha);
        add(UI.espaco(10));

        add(venn);
        add(UI.espaco(10));

        definicao.setPreferredSize(new Dimension(768, 66));
        definicao.setMaximumSize(new Dimension(768, 66));
        definicao.setVerticalAlignment(SwingConstants.TOP);
        add(definicao);
        add(UI.espaco(10));
        resultado.setPreferredSize(new Dimension(768, 48));
        resultado.setMaximumSize(new Dimension(768, 48));
        add(resultado);

        sortear();
        atualizar();
    }

    private void adicionarOp(Bloco linha, String texto, VennPanel.Op op) {
        Botao bt = new Botao(texto, Cores.CIANO);
        bt.setToggle(true);
        bt.setBorder(new EmptyBorder(12, 20, 12, 20));
        bt.addActionListener(e -> {
            atual = op;
            atualizar();
        });
        botoes.add(bt);
        linha.add(bt);
    }

    private void sortear() {
        do {
            a.clear();
            b.clear();
            int na = 3 + rnd.nextInt(3), nb = 3 + rnd.nextInt(3);
            while (a.size() < na) a.add(1 + rnd.nextInt(9));
            while (b.size() < nb) b.add(1 + rnd.nextInt(9));
        } while (!valido());
    }

    private boolean valido() {
        Set<Integer> i = VennPanel.calcular(VennPanel.Op.INTERSECCAO, a, b);
        return i.size() >= 1 && i.size() <= 3
                && !VennPanel.calcular(VennPanel.Op.A_MENOS_B, a, b).isEmpty()
                && !VennPanel.calcular(VennPanel.Op.B_MENOS_A, a, b).isEmpty();
    }

    private String formatar(Set<Integer> s) {
        if (s.isEmpty()) return "∅ (conjunto vazio)";
        StringBuilder sb = new StringBuilder("{");
        boolean primeiro = true;
        for (Integer n : s) {
            if (!primeiro) sb.append(", ");
            sb.append(n);
            primeiro = false;
        }
        return sb.append("}").toString();
    }

    private void atualizar() {
        venn.setDados(a, b);
        venn.setOp(atual);
        VennPanel.Op[] ordem = {VennPanel.Op.UNIAO, VennPanel.Op.INTERSECCAO, VennPanel.Op.A_MENOS_B, VennPanel.Op.B_MENOS_A};
        for (int i = 0; i < botoes.size(); i++) botoes.get(i).setSelecionado(ordem[i] == atual);

        lblConjuntos.definir("<font color='" + UI.hex(Cores.AZUL) + "'>A = " + formatar(a)
                + "</font> &nbsp;&nbsp;&nbsp; <font color='" + UI.hex(Cores.ROXO) + "'>B = " + formatar(b) + "</font>");

        String nome, def;
        switch (atual) {
            case INTERSECCAO:
                nome = "A ∩ B";
                def = "<b>Interseção</b>: só os elementos que estão em A <i>e</i> em B ao mesmo tempo.";
                break;
            case A_MENOS_B:
                nome = "A − B";
                def = "<b>Diferença</b>: os elementos que estão em A, mas <i>não</i> estão em B.";
                break;
            case B_MENOS_A:
                nome = "B − A";
                def = "<b>Diferença</b>: os elementos que estão em B, mas <i>não</i> estão em A.";
                break;
            default:
                nome = "A ∪ B";
                def = "<b>União</b>: todos os elementos que estão em A <i>ou</i> em B (sem repetir).";
        }
        definicao.mostrar(def, 730, Cores.CARTAO_CLARO);
        resultado.mostrar("<b>" + nome + " = " + formatar(VennPanel.calcular(atual, a, b)) + "</b>", 730, Cores.alfa(Cores.CIANO, 40));
    }
}

class TelaOperacoes extends Pagina {
    TelaOperacoes(Consumer<String> nav) {
        super("Módulo 2", "Operações com Conjuntos",
                "Combinando conjuntos criamos novos conjuntos. Teste no laboratório e veja o diagrama de Venn mudar na hora.",
                Cores.ROXO);

        adicionar(new LaboratorioConjuntos());

        secao("As operações");
        Bloco grade = new Bloco(new GridLayout(2, 2, 16, 16));
        grade.add(operacao("A ∪ B", "União (A ∪ B)", "Junta os elementos dos dois conjuntos, sem repetir. Pense em <b>OU</b>.",
                "{1, 2} ∪ {2, 3} = {1, 2, 3}", Cores.AZUL));
        grade.add(operacao("A ∩ B", "Interseção (A ∩ B)", "Fica só com o que é comum aos dois. Pense em <b>E</b>.",
                "{1, 2} ∩ {2, 3} = {2}", Cores.ROXO));
        grade.add(operacao("A − B", "Diferença (A − B)", "Elementos de A que <b>não</b> estão em B. A ordem importa!",
                "{1, 2, 3} − {3, 4} = {1, 2}", Cores.CIANO));
        grade.add(operacao("A′", "Complementar (A′)", "Tudo que está no universo U, mas fora de A: A′ = U − A.",
                "U = {1, 2, 3, 4, 5} e A = {1, 2} → A′ = {3, 4, 5}", Cores.VERDE));
        adicionar(grade);

        Cartao formula = new Cartao(Cores.AMARELO);
        formula.add(UI.rotulo("Contando elementos da união", Cores.SUBTITULO, Cores.AMARELO));
        formula.add(UI.espaco(10));
        formula.add(UI.rotulo("n(A ∪ B) = n(A) + n(B) − n(A ∩ B)", new Font("Dialog", Font.BOLD, 26), Cores.TEXTO));
        formula.add(UI.espaco(10));
        formula.add(UI.texto("Subtraímos a interseção porque os elementos comuns foram contados duas vezes.", UI.TEXTO_L, Cores.TEXTO_SECUNDARIO));
        formula.add(UI.espaco(12));
        formula.add(new Caixa("<b>Problema:</b> numa turma, 20 alunos gostam de futebol, 15 de vôlei e 8 gostam dos dois. "
                + "Quantos gostam de pelo menos um dos esportes?<br><b>Solução:</b> 20 + 15 − 8 = <b>27 alunos</b>.", 700, Cores.CARTAO_CLARO));
        adicionar(formula);

        Botao praticar = new Botao("Praticar Operações  \u2192", Cores.ROXO);
        praticar.addActionListener(e -> nav.accept("atividades:1"));
        adicionar(praticar);
    }

    private Cartao operacao(String simbolo, String nome, String desc, String exemplo, Color cor) {
        Cartao c = new Cartao(cor, 16);
        c.setBorder(new EmptyBorder(16, 24, 22, 16));
        c.add(UI.rotulo(simbolo, new Font("Dialog", Font.BOLD, 32), cor));
        c.add(UI.rotulo(nome, new Font("Dialog", Font.BOLD, 19), Cores.TEXTO));
        c.add(UI.espaco(6));
        c.add(UI.texto(desc, 340, Cores.TEXTO_SECUNDARIO));
        c.add(UI.espaco(10));
        c.add(new Caixa("<b>Exemplo:</b><br>" + exemplo, 310, Cores.CARTAO_CLARO));
        return c;
    }
}

// ==========================================================
// TELA: TABELAS-VERDADE (MÓDULO 3)
// ==========================================================
class TabelaVerdade {
    private static final boolean[][] ENTRADAS = {{true, true}, {true, false}, {false, true}, {false, false}};

    static JPanel binaria(String resultado, Color cor, BiFunction<Boolean, Boolean, Boolean> f) {
        boolean[][] dados = new boolean[4][3];
        for (int i = 0; i < 4; i++) {
            dados[i][0] = ENTRADAS[i][0];
            dados[i][1] = ENTRADAS[i][1];
            dados[i][2] = f.apply(ENTRADAS[i][0], ENTRADAS[i][1]);
        }
        return montar(new String[]{"P", "Q", resultado}, dados, cor);
    }

    static JPanel unaria(String resultado, Color cor, Function<Boolean, Boolean> f) {
        boolean[][] dados = {{true, f.apply(true)}, {false, f.apply(false)}};
        return montar(new String[]{"P", resultado}, dados, cor);
    }

    private static JPanel montar(String[] cab, boolean[][] dados, Color cor) {
        int cols = cab.length;
        JPanel p = new JPanel(new GridLayout(dados.length + 1, cols, 4, 4));
        p.setOpaque(false);
        for (int c = 0; c < cols; c++) {
            boolean res = c == cols - 1;
            p.add(new Celula(cab[c], res ? Cores.alfa(cor, 130) : Cores.CARTAO_CLARO, Color.WHITE));
        }
        for (boolean[] linha : dados) {
            for (int c = 0; c < cols; c++) {
                boolean v = linha[c];
                p.add(new Celula(v ? "V" : "F",
                        Cores.alfa(v ? Cores.VERDE : Cores.VERMELHO, 55),
                        v ? Cores.VERDE : Cores.VERMELHO));
            }
        }
        int w = cols * 80 + (cols - 1) * 4;
        int h = (dados.length + 1) * 38 + dados.length * 4;
        p.setPreferredSize(new Dimension(w, h));
        p.setMaximumSize(new Dimension(w, h));
        return p;
    }
}

class Ficha extends JPanel {
    private final String titulo;
    private boolean valor;

    Ficha(String titulo) {
        this.titulo = titulo;
        setOpaque(false);
        setPreferredSize(new Dimension(140, 96));
    }

    void setValor(boolean v) {
        valor = v;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        Color cor = valor ? Cores.VERDE : Cores.VERMELHO;
        g2.setColor(Cores.CARTAO_CLARO);
        g2.fill(UI.rr(0, 0, w, h, 14));
        Shape antigo = g2.getClip();
        g2.setClip(UI.rr(0, 0, w, h, 14));
        g2.setColor(cor);
        g2.fillRect(0, h - 5, w, 5);
        g2.setClip(antigo);
        g2.setColor(Cores.TEXTO_SECUNDARIO);
        g2.setFont(new Font("Dialog", Font.BOLD, 16));
        UI.centralizar(g2, titulo, w / 2.0, 24);
        g2.setColor(cor);
        g2.setFont(new Font("Dialog", Font.BOLD, 42));
        UI.centralizar(g2, valor ? "V" : "F", w / 2.0, 58);
        g2.dispose();
    }
}

class LaboratorioLogico extends Cartao {
    private boolean p = true, q = false;
    private final Botao bp = new Botao("", Cores.VERDE);
    private final Botao bq = new Botao("", Cores.VERMELHO);
    private final Ficha[] fichas = {new Ficha("¬P"), new Ficha("P ∧ Q"), new Ficha("P ∨ Q"), new Ficha("P → Q"), new Ficha("P ↔ Q")};

    LaboratorioLogico() {
        super(Cores.CIANO);
        add(UI.rotulo("Laboratório Lógico", Cores.SUBTITULO, Cores.CIANO));
        add(UI.espaco(4));
        add(UI.texto("Clique nos botões para mudar os valores de P e Q e veja o resultado de todas as operações.",
                UI.TEXTO_L, Cores.TEXTO_SECUNDARIO));
        add(UI.espaco(14));

        Bloco linha = new Bloco(new FlowLayout(FlowLayout.LEFT, 12, 0));
        bp.setPreferredSize(new Dimension(210, 46));
        bq.setPreferredSize(new Dimension(210, 46));
        bp.addActionListener(e -> { p = !p; atualizar(); });
        bq.addActionListener(e -> { q = !q; atualizar(); });
        linha.add(bp);
        linha.add(bq);
        add(linha);
        add(UI.espaco(14));

        Bloco grade = new Bloco(new GridLayout(1, 5, 12, 0));
        for (Ficha f : fichas) grade.add(f);
        add(grade);
        atualizar();
    }

    private void atualizar() {
        bp.setText("P = " + (p ? "Verdadeiro" : "Falso"));
        bp.setCor(p ? Cores.VERDE : Cores.VERMELHO);
        bq.setText("Q = " + (q ? "Verdadeiro" : "Falso"));
        bq.setCor(q ? Cores.VERDE : Cores.VERMELHO);
        fichas[0].setValor(!p);
        fichas[1].setValor(p && q);
        fichas[2].setValor(p || q);
        fichas[3].setValor(!p || q);
        fichas[4].setValor(p == q);
    }
}

class TelaTabelasVerdade extends Pagina {
    TelaTabelasVerdade(Consumer<String> nav) {
        super("Módulo 3", "Lógica e Tabelas-Verdade",
                "A lógica ensina a raciocinar com clareza. Uma tabela-verdade mostra, para cada combinação de V e F, o resultado de uma afirmação.",
                Cores.CIANO);

        String v = UI.hex(Cores.VERDE), f = UI.hex(Cores.VERMELHO);
        adicionar(UI.cartaoTexto("O que é uma proposição?", Cores.CIANO,
                "Proposição é uma frase que afirma algo e pode ser classificada como <b>Verdadeira (V)</b> ou <b>Falsa (F)</b>, nunca as duas.<br><br>"
                        + "• “7 é um número primo.” → <font color='" + v + "'><b>V</b></font><br>"
                        + "• “5 é maior que 9.” → <font color='" + f + "'><b>F</b></font><br>"
                        + "• “Que horas são?” → <b>não é proposição</b> (é pergunta)<br>"
                        + "• “Feche a porta!” → <b>não é proposição</b> (é ordem)",
                UI.TEXTO_L));

        adicionar(new LaboratorioLogico());

        secao("Os conectivos lógicos");
        adicionar(operador("Negação", "¬P", "lê-se “não P”",
                "Inverte o valor lógico: o que era V vira F e vice-versa.",
                "P: “Está chovendo.”<br>¬P: “Não está chovendo.”", Cores.ROXO,
                TabelaVerdade.unaria("¬P", Cores.ROXO, x -> !x)));
        adicionar(operador("Conjunção", "P ∧ Q", "lê-se “P e Q”",
                "Só é verdadeira quando <b>as duas</b> forem verdadeiras.",
                "“Vou ao cinema <b>e</b> compro pipoca.” Só cumpro se fizer as duas coisas.", Cores.AZUL,
                TabelaVerdade.binaria("P ∧ Q", Cores.AZUL, (x, y) -> x && y)));
        adicionar(operador("Disjunção", "P ∨ Q", "lê-se “P ou Q”",
                "É verdadeira quando <b>pelo menos uma</b> for verdadeira. Só é falsa se as duas forem falsas.",
                "“Vou de ônibus <b>ou</b> de bicicleta.” Basta uma das opções.", Cores.CIANO,
                TabelaVerdade.binaria("P ∨ Q", Cores.CIANO, (x, y) -> x || y)));
        adicionar(operador("Condicional", "P → Q", "lê-se “se P, então Q”",
                "Só é <b>falsa</b> quando P é verdadeira e Q é falsa (a promessa foi quebrada).",
                "“Se eu passar, ganho um presente.” Se não passei, a promessa não foi quebrada, logo é V.", Cores.AMARELO,
                TabelaVerdade.binaria("P → Q", Cores.AMARELO, (x, y) -> !x || y)));
        adicionar(operador("Bicondicional", "P ↔ Q", "lê-se “P se, e somente se, Q”",
                "É verdadeira quando P e Q têm <b>o mesmo valor</b> (as duas V ou as duas F).",
                "“Um número é par se, e somente se, é divisível por 2.”", Cores.VERDE,
                TabelaVerdade.binaria("P ↔ Q", Cores.VERDE, (x, y) -> x.booleanValue() == y.booleanValue())));

        Botao praticar = new Botao("Praticar Lógica  \u2192", Cores.CIANO);
        praticar.addActionListener(e -> nav.accept("atividades:2"));
        adicionar(praticar);
    }

    private Cartao operador(String nome, String simbolo, String leitura, String regra, String exemplo, Color cor, JPanel tabela) {
        Cartao c = new Cartao(cor);
        Bloco linha = new Bloco(new BorderLayout(24, 0));
        Bloco textos = new Bloco();
        textos.add(UI.rotulo(nome + "   " + simbolo, Cores.SUBTITULO, cor));
        textos.add(UI.espaco(2));
        textos.add(UI.rotulo(leitura, Cores.TEXTO_PEQUENO, Cores.TEXTO_SECUNDARIO));
        textos.add(UI.espaco(10));
        textos.add(UI.texto(regra, 480));
        textos.add(UI.espaco(10));
        textos.add(new Caixa("<b>Exemplo:</b> " + exemplo, 450, Cores.CARTAO_CLARO));
        linha.add(textos, BorderLayout.CENTER);

        JPanel lado = new JPanel(new BorderLayout());
        lado.setOpaque(false);
        lado.add(tabela, BorderLayout.NORTH);
        linha.add(lado, BorderLayout.EAST);
        c.add(linha);
        return c;
    }
}

// ==========================================================
// QUESTÕES E QUIZ
// ==========================================================
class Questao {
    final String pergunta;
    final String[] opcoes;
    final int correta;
    final String explicacao;
    final boolean fixa;

    Questao(String p, String[] o, int c, String e) { this(p, o, c, e, false); }

    Questao(String p, String[] o, int c, String e, boolean fixa) {
        this.pergunta = p;
        this.opcoes = o;
        this.correta = c;
        this.explicacao = e;
        this.fixa = fixa;
    }

    Questao embaralhada() {
        if (fixa) return this;
        List<String> l = new ArrayList<>(Arrays.asList(opcoes));
        String certa = opcoes[correta];
        Collections.shuffle(l);
        return new Questao(pergunta, l.toArray(new String[0]), l.indexOf(certa), explicacao, false);
    }
}

class BancoQuestoes {
    static List<Questao> conjuntos() {
        List<Questao> l = new ArrayList<>();
        l.add(new Questao("Considere A = {1, 2, 3, 4}. Qual número NÃO pertence a A?",
                new String[]{"1", "2", "3", "5"}, 3,
                "O 5 não está listado em A, então 5 ∉ A."));
        l.add(new Questao("Qual símbolo indica que um elemento pertence a um conjunto?",
                new String[]{"∈", "∉", "⊂", "∪"}, 0,
                "∈ liga elemento a conjunto. O símbolo ⊂ liga conjunto a conjunto."));
        l.add(new Questao("Se A = {1, 2} e B = {1, 2, 3, 4}, qual afirmação é verdadeira?",
                new String[]{"A ⊂ B", "B ⊂ A", "3 ∈ A", "A = B"}, 0,
                "Todo elemento de A também está em B, portanto A ⊂ B."));
        l.add(new Questao("Quantos elementos tem C = {a, e, i, o, u}? Ou seja, qual é n(C)?",
                new String[]{"4", "5", "6", "1"}, 1,
                "Contando: a, e, i, o, u. Logo n(C) = 5."));
        l.add(new Questao("Qual dos conjuntos abaixo é vazio?",
                new String[]{"{0}", "{ }", "{1, 2}", "{a}"}, 1,
                "{0} tem um elemento (o zero), então não é vazio. O vazio é { } ou ∅."));
        return l;
    }

    static List<Questao> operacoes() {
        List<Questao> l = new ArrayList<>();
        l.add(new Questao("Dados A = {1, 2, 3} e B = {3, 4, 5}, qual é A ∪ B?",
                new String[]{"{3}", "{1, 2, 3, 4, 5}", "{1, 2}", "{4, 5}"}, 1,
                "A união junta todos os elementos de A e de B, sem repetir o 3."));
        l.add(new Questao("Sendo A = {1, 2, 3} e B = {2, 3, 4}, qual é A ∩ B?",
                new String[]{"{1, 4}", "{2, 3}", "{1, 2, 3, 4}", "{1}"}, 1,
                "A interseção tem só os elementos comuns: 2 e 3."));
        l.add(new Questao("Sendo A = {1, 2, 3, 4} e B = {3, 4, 5}, qual é A − B?",
                new String[]{"{5}", "{3, 4}", "{1, 2}", "{1, 2, 5}"}, 2,
                "A − B são os elementos de A que não estão em B: 1 e 2."));
        l.add(new Questao("20 alunos gostam de futebol, 15 de vôlei e 8 gostam dos dois. Quantos gostam de pelo menos um?",
                new String[]{"43", "35", "27", "12"}, 2,
                "n(A ∪ B) = 20 + 15 − 8 = 27. Subtraímos 8 porque foram contados duas vezes."));
        l.add(new Questao("Se A ∩ B = ∅, dizemos que A e B são conjuntos...",
                new String[]{"iguais", "disjuntos", "unitários", "infinitos"}, 1,
                "Conjuntos sem nenhum elemento em comum são chamados de disjuntos."));
        return l;
    }

    static List<Questao> logica() {
        List<Questao> l = new ArrayList<>();
        l.add(new Questao("Quando a conjunção P ∧ Q é verdadeira?",
                new String[]{"Quando P é falsa", "Quando Q é falsa", "Quando pelo menos uma é verdadeira", "Quando as duas são verdadeiras"}, 3,
                "A conjunção (E) exige que P e Q sejam verdadeiras ao mesmo tempo."));
        l.add(new Questao("Se P = Falso e Q = Verdadeiro, qual é o valor de P ∨ Q?",
                new String[]{"Verdadeiro", "Falso"}, 0,
                "Na disjunção (OU), basta uma ser verdadeira. Como Q é V, o resultado é V.", true));
        l.add(new Questao("Qual é o valor de ¬P quando P é verdadeira?",
                new String[]{"Verdadeiro", "Falso"}, 1,
                "A negação inverte o valor: se P é V, então ¬P é F.", true));
        l.add(new Questao("A condicional P → Q só é falsa quando...",
                new String[]{"P é falsa e Q é verdadeira", "P é verdadeira e Q é falsa", "P e Q são falsas", "P e Q são verdadeiras"}, 1,
                "Só há promessa quebrada quando P acontece (V) e Q não acontece (F)."));
        l.add(new Questao("Qual das frases abaixo é uma proposição?",
                new String[]{"Que horas são?", "Feche a porta!", "7 é um número primo.", "Quem quer sorvete?"}, 2,
                "Proposição é uma frase declarativa que pode ser V ou F. As outras são pergunta ou ordem."));
        l.add(new Questao("A bicondicional P ↔ Q é verdadeira quando...",
                new String[]{"P e Q têm o mesmo valor lógico", "P e Q têm valores diferentes", "Apenas P é verdadeira", "Apenas Q é verdadeira"}, 0,
                "P ↔ Q é V quando P e Q são ambas V ou ambas F."));
        return l;
    }

    static List<Questao> todas() {
        List<Questao> l = new ArrayList<>();
        l.addAll(conjuntos());
        l.addAll(operacoes());
        l.addAll(logica());
        return l;
    }
}

class QuizPanel extends Cartao {
    private final List<Questao> banco;
    private final int limite;
    private List<Questao> fila = new ArrayList<>();
    private int idx, acertos;
    private boolean respondido;

    private final CardLayout cl = new CardLayout();
    private final JPanel raiz = new JPanel(cl);
    private final JLabel lblProgresso;
    private final Texto lblPergunta = new Texto("", new Font("Dialog", Font.BOLD, 21), Cores.TEXTO, 740);
    private final JLabel lblFimPct = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblFimMsg = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblFimDet = new JLabel("", SwingConstants.CENTER);
    private final BarraProgresso barra;
    private final OpcaoResposta[] opcoes = new OpcaoResposta[4];
    private final Caixa feedback;
    private final Botao proximo;
    private final Bloco linhaProximo;

    QuizPanel(String titulo, List<Questao> banco, int limite, Color cor) {
        super(cor);
        this.banco = banco;
        this.limite = limite;
        setBorder(new EmptyBorder(24, 30, 30, 30));
        raiz.setOpaque(false);
        raiz.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- visão do quiz ----
        Bloco quiz = new Bloco();
        Bloco topo = new Bloco(new BorderLayout());
        topo.add(UI.rotulo(titulo.toUpperCase(), Cores.ETIQUETA, cor), BorderLayout.WEST);
        lblProgresso = UI.rotulo("", Cores.ETIQUETA, Cores.TEXTO_SECUNDARIO);
        topo.add(lblProgresso, BorderLayout.EAST);
        quiz.add(topo);
        quiz.add(UI.espaco(10));
        barra = new BarraProgresso(cor);
        quiz.add(barra);
        quiz.add(UI.espaco(22));

        Bloco pw = new Bloco(new BorderLayout());
        pw.setPreferredSize(new Dimension(760, 74));
        pw.add(lblPergunta, BorderLayout.NORTH);
        quiz.add(pw);
        quiz.add(UI.espaco(8));

        for (int i = 0; i < 4; i++) {
            final int k = i;
            opcoes[i] = new OpcaoResposta();
            opcoes[i].addActionListener(e -> responder(k));
            quiz.add(opcoes[i]);
            quiz.add(UI.espaco(10));
        }

        feedback = new Caixa(null, 730, Cores.CARTAO_CLARO);
        feedback.setVerticalAlignment(SwingConstants.TOP);
        feedback.setPreferredSize(new Dimension(760, 104));
        feedback.setMaximumSize(new Dimension(760, 104));
        quiz.add(feedback);

        proximo = new Botao("Próxima questão  \u2192", cor);
        proximo.setVisible(false);
        proximo.addActionListener(e -> avancar());
        linhaProximo = new Bloco(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        linhaProximo.setPreferredSize(new Dimension(760, 66));
        linhaProximo.add(proximo);
        quiz.add(linhaProximo);

        // ---- visão final ----
        Bloco fim = new Bloco();
        fim.add(Box.createVerticalGlue());
        lblFimPct.setFont(new Font("Dialog", Font.BOLD, 72));
        lblFimMsg.setFont(new Font("Dialog", Font.BOLD, 22));
        lblFimMsg.setForeground(Cores.TEXTO);
        lblFimDet.setFont(Cores.TEXTO_NORMAL);
        lblFimDet.setForeground(Cores.TEXTO_SECUNDARIO);
        Botao refazer = new Botao("Tentar novamente", cor);
        refazer.setAlignmentX(Component.CENTER_ALIGNMENT);
        refazer.addActionListener(e -> iniciar());
        for (JLabel l : new JLabel[]{lblFimPct, lblFimMsg, lblFimDet}) l.setAlignmentX(Component.CENTER_ALIGNMENT);
        fim.add(lblFimPct);
        fim.add(UI.espaco(6));
        fim.add(lblFimMsg);
        fim.add(UI.espaco(8));
        fim.add(lblFimDet);
        fim.add(UI.espaco(24));
        fim.add(refazer);
        fim.add(Box.createVerticalGlue());

        raiz.add(quiz, "quiz");
        raiz.add(fim, "fim");
        add(raiz);
        iniciar();
    }

    private void iniciar() {
        fila = new ArrayList<>();
        for (Questao q : banco) fila.add(q.embaralhada());
        Collections.shuffle(fila);
        if (limite > 0 && fila.size() > limite) fila = new ArrayList<>(fila.subList(0, limite));
        idx = 0;
        acertos = 0;
        barra.setValor(0);
        mostrar();
    }

    private void mostrar() {
        if (idx >= fila.size()) {
            mostrarFim();
            return;
        }
        Questao q = fila.get(idx);
        respondido = false;
        lblProgresso.setText("Questão " + (idx + 1) + " de " + fila.size());
        barra.setValor(idx / (double) fila.size());
        lblPergunta.definir(q.pergunta);
        String[] letras = {"A", "B", "C", "D"};
        for (int i = 0; i < 4; i++) {
            if (i < q.opcoes.length) {
                opcoes[i].configurar(letras[i], q.opcoes[i]);
                opcoes[i].setVisible(true);
            } else {
                opcoes[i].setVisible(false);
            }
        }
        feedback.limpar();
        proximo.setVisible(false);
        cl.show(raiz, "quiz");
        revalidate();
        repaint();
    }

    private void responder(int escolhida) {
        if (respondido) return;
        respondido = true;
        Questao q = fila.get(idx);
        boolean ok = escolhida == q.correta;
        Pontuacao.registrarResposta(ok);
        if (ok) acertos++;
        for (int i = 0; i < q.opcoes.length; i++) {
            if (i == q.correta) opcoes[i].setEstado(OpcaoResposta.Estado.CORRETA);
            else if (i == escolhida) opcoes[i].setEstado(OpcaoResposta.Estado.ERRADA);
            else opcoes[i].setEstado(OpcaoResposta.Estado.APAGADA);
        }
        String letras = "ABCD";
        if (ok) {
            feedback.mostrar("<b>Correto!</b> +10 pontos.<br>" + q.explicacao, 730, Cores.alfa(Cores.VERDE, 40));
        } else {
            feedback.mostrar("<b>Quase!</b> A resposta certa é <b>" + letras.charAt(q.correta) + ") "
                    + q.opcoes[q.correta] + "</b>.<br>" + q.explicacao, 730, Cores.alfa(Cores.VERMELHO, 40));
        }
        proximo.setText(idx == fila.size() - 1 ? "Ver resultado" : "Próxima questão  \u2192");
        proximo.setVisible(true);
        linhaProximo.revalidate();
        linhaProximo.repaint();
    }

    private void avancar() {
        idx++;
        mostrar();
    }

    private void mostrarFim() {
        barra.setValor(1);
        lblProgresso.setText("Concluído");
        int pct = (int) Math.round(100.0 * acertos / fila.size());
        lblFimPct.setText(pct + "%");
        lblFimPct.setForeground(pct >= 70 ? Cores.VERDE : (pct >= 40 ? Cores.AMARELO : Cores.VERMELHO));
        if (pct >= 80) lblFimMsg.setText("Excelente! Você domina o conteúdo.");
        else if (pct >= 50) lblFimMsg.setText("Bom trabalho! Revise o que errou e tente de novo.");
        else lblFimMsg.setText("Releia a teoria e tente novamente. Errar faz parte de aprender!");
        lblFimDet.setText("Você acertou " + acertos + " de " + fila.size() + " questões.");
        cl.show(raiz, "fim");
    }
}

// ==========================================================
// DESAFIO: COMPLETE A TABELA-VERDADE
// ==========================================================
class DesafioTabela extends Cartao {
    private static final String[] NOMES = {"P ∧ Q  (P e Q)", "P ∨ Q  (P ou Q)", "P → Q  (se P, então Q)", "P ↔ Q  (P se, e somente se, Q)"};
    private static final String[] CABECALHOS = {"P ∧ Q", "P ∨ Q", "P → Q", "P ↔ Q"};
    private static final List<BiFunction<Boolean, Boolean, Boolean>> FUNCOES = Arrays.<BiFunction<Boolean, Boolean, Boolean>>asList(
            (p, q) -> p && q,
            (p, q) -> p || q,
            (p, q) -> !p || q,
            (p, q) -> p.booleanValue() == q.booleanValue());
    private static final boolean[][] ENTRADAS = {{true, true}, {true, false}, {false, true}, {false, false}};

    private final CelulaTabela[] celulas = new CelulaTabela[4];
    private final JLabel lblOp = new JLabel();
    private final Celula cabecalhoResultado = new Celula("", Cores.alfa(Cores.AMARELO, 130), Color.WHITE);
    private final Caixa msg = new Caixa(null, 730, Cores.CARTAO_CLARO);
    private final Random rnd = new Random();
    private int op = -1;
    private boolean pontuado;

    DesafioTabela() {
        super(Cores.AMARELO);
        setBorder(new EmptyBorder(24, 30, 30, 30));
        add(UI.rotulo("DESAFIO", Cores.ETIQUETA, Cores.AMARELO));
        add(UI.espaco(6));
        add(UI.rotulo("Complete a tabela-verdade", new Font("Dialog", Font.BOLD, 24), Cores.TEXTO));
        add(UI.espaco(8));
        lblOp.setFont(new Font("Dialog", Font.BOLD, 20));
        lblOp.setForeground(Cores.AMARELO);
        lblOp.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblOp);
        add(UI.espaco(6));
        add(UI.texto("Clique nas células amarelas da última coluna para alternar entre <b>?</b>, <b>V</b> e <b>F</b>. "
                + "Depois clique em <b>Verificar</b>.", 740, Cores.TEXTO_SECUNDARIO));
        add(UI.espaco(16));

        JPanel tabela = new JPanel(new GridLayout(5, 3, 6, 6));
        tabela.setOpaque(false);
        tabela.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabela.add(new Celula("P", Cores.CARTAO_CLARO, Color.WHITE));
        tabela.add(new Celula("Q", Cores.CARTAO_CLARO, Color.WHITE));
        tabela.add(cabecalhoResultado);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 2; c++) {
                boolean v = ENTRADAS[r][c];
                tabela.add(new Celula(v ? "V" : "F", Cores.alfa(v ? Cores.VERDE : Cores.VERMELHO, 55), v ? Cores.VERDE : Cores.VERMELHO));
            }
            celulas[r] = new CelulaTabela();
            tabela.add(celulas[r]);
        }
        tabela.setPreferredSize(new Dimension(300, 250));
        tabela.setMaximumSize(new Dimension(300, 250));
        add(tabela);
        add(UI.espaco(16));

        Bloco botoes = new Bloco(new FlowLayout(FlowLayout.LEFT, 12, 0));
        Botao verificar = new Botao("Verificar", Cores.VERDE);
        verificar.addActionListener(e -> verificar());
        Botao novo = new Botao("Novo desafio", Cores.ROXO);
        novo.addActionListener(e -> novo());
        botoes.add(verificar);
        botoes.add(novo);
        add(botoes);
        add(UI.espaco(14));

        msg.setVerticalAlignment(SwingConstants.TOP);
        msg.setPreferredSize(new Dimension(760, 70));
        msg.setMaximumSize(new Dimension(760, 70));
        add(msg);
        novo();
    }

    private void novo() {
        int anterior = op;
        do {
            op = rnd.nextInt(4);
        } while (op == anterior);
        lblOp.setText("Operação: " + NOMES[op]);
        cabecalhoResultado.setText(CABECALHOS[op]);
        for (CelulaTabela c : celulas) c.reset();
        msg.limpar();
        pontuado = false;
    }

    private void verificar() {
        for (CelulaTabela c : celulas) {
            if (c.estado == 0) {
                msg.mostrar("Preencha <b>todas</b> as células da última coluna antes de verificar.", 730, Cores.alfa(Cores.AMARELO, 40));
                return;
            }
        }
        int certas = 0;
        for (int r = 0; r < 4; r++) {
            boolean esperado = FUNCOES.get(op).apply(ENTRADAS[r][0], ENTRADAS[r][1]);
            boolean marcado = celulas[r].estado == 1;
            boolean ok = esperado == marcado;
            celulas[r].verificado = ok ? 1 : 2;
            celulas[r].repaint();
            if (ok) certas++;
        }
        if (certas == 4) {
            if (!pontuado) {
                Pontuacao.registrarResposta(true);
                pontuado = true;
                msg.mostrar("<b>Perfeito!</b> +10 pontos. Clique em <b>Novo desafio</b> para continuar.", 730, Cores.alfa(Cores.VERDE, 40));
            } else {
                msg.mostrar("<b>Perfeito!</b> Clique em <b>Novo desafio</b> para continuar.", 730, Cores.alfa(Cores.VERDE, 40));
            }
        } else {
            if (!pontuado) {
                Pontuacao.registrarResposta(false);
                pontuado = true;
            }
            msg.mostrar("Você acertou <b>" + certas + " de 4</b> células. As com borda vermelha estão erradas. "
                    + "Corrija e verifique de novo, ou peça um novo desafio.", 730, Cores.alfa(Cores.VERMELHO, 40));
        }
    }
}

// ==========================================================
// TELA: ATIVIDADES
// ==========================================================
class TelaAtividades extends Pagina {
    private final CardLayout cl = new CardLayout();
    private final JPanel area = new JPanel(cl) {
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(UI.LARGURA, getPreferredSize().height);
        }
    };
    private final List<Botao> abas = new ArrayList<>();

    TelaAtividades() {
        super("Pratique", "Atividades",
                "Escolha um tema e responda. Você recebe a explicação de cada questão e ganha 10 pontos por acerto.",
                Cores.AMARELO);

        String[] nomes = {"Conjuntos", "Operações", "Lógica", "Desafio da Tabela"};
        Color[] cores = {Cores.AZUL, Cores.ROXO, Cores.CIANO, Cores.AMARELO};
        Bloco linha = new Bloco(new FlowLayout(FlowLayout.LEFT, 10, 0));
        for (int i = 0; i < nomes.length; i++) {
            final int k = i;
            Botao b = new Botao(nomes[i], cores[i]);
            b.setToggle(true);
            b.addActionListener(e -> selecionar(k));
            abas.add(b);
            linha.add(b);
        }
        adicionar(linha);

        area.setOpaque(false);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        area.add(new QuizPanel("Conjuntos", BancoQuestoes.conjuntos(), 0, Cores.AZUL), "0");
        area.add(new QuizPanel("Operações", BancoQuestoes.operacoes(), 0, Cores.ROXO), "1");
        area.add(new QuizPanel("Lógica", BancoQuestoes.logica(), 0, Cores.CIANO), "2");
        area.add(new DesafioTabela(), "3");
        adicionar(area);
        selecionar(0);
    }

    void selecionar(int i) {
        for (int j = 0; j < abas.size(); j++) abas.get(j).setSelecionado(j == i);
        cl.show(area, String.valueOf(i));
    }
}

// ==========================================================
// TELA: SIMULADO
// ==========================================================
class TelaProva extends Pagina {
    TelaProva() {
        super("Hora do desafio", "Simulado",
                "Dez questões sorteadas de todos os módulos. Mostre o que você aprendeu!",
                Cores.VERDE);
        adicionar(new Caixa("<b>Como funciona:</b> cada acerto vale <b>10 pontos</b>. Depois de responder você vê a explicação. "
                + "No final, confira sua nota e tente de novo com novas questões.", 770, Cores.CARTAO));
        adicionar(new QuizPanel("Simulado de Lógica e Conjuntos", BancoQuestoes.todas(), 10, Cores.VERDE));
    }
}

// ==========================================================
// TELA: DESEMPENHO
// ==========================================================
class Medidor extends JPanel {
    private double atual = 0, alvo = 0;
    private boolean vazio = true;

    Medidor() {
        setOpaque(false);
        setPreferredSize(new Dimension(210, 210));
        Timer t = new Timer(16, e -> {
            atual += (alvo - atual) * 0.12;
            if (Math.abs(alvo - atual) < 0.1) {
                atual = alvo;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        addPropertyChangeListener("alvo", ev -> t.restart());
    }

    void setValor(double pct, boolean vazio) {
        this.vazio = vazio;
        double antigo = alvo;
        alvo = pct;
        firePropertyChange("alvo", antigo, pct + (vazio ? 1000 : 0));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UI.suavizar(g2);
        int w = getWidth(), h = getHeight();
        double s = Math.min(w, h) - 30;
        double x = (w - s) / 2, y = (h - s) / 2;
        g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Cores.CARTAO_CLARO);
        g2.draw(new Ellipse2D.Double(x, y, s, s));
        Color cor = vazio ? Cores.BORDA : (alvo >= 70 ? Cores.VERDE : (alvo >= 40 ? Cores.AMARELO : Cores.VERMELHO));
        if (!vazio && atual > 0) {
            g2.setColor(cor);
            g2.draw(new Arc2D.Double(x, y, s, s, 90, -360 * atual / 100.0, Arc2D.OPEN));
        }
        g2.setColor(vazio ? Cores.TEXTO_SECUNDARIO : Cores.TEXTO);
        g2.setFont(new Font("Dialog", Font.BOLD, 44));
        UI.centralizar(g2, vazio ? "—" : Math.round(atual) + "%", w / 2.0, h / 2.0 - 8);
        g2.setColor(Cores.TEXTO_SECUNDARIO);
        g2.setFont(Cores.TEXTO_PEQUENO);
        UI.centralizar(g2, "aproveitamento", w / 2.0, h / 2.0 + 28);
        g2.dispose();
    }
}

class TelaDesempenho extends Pagina {
    private final JLabel vPontos = new JLabel("0");
    private final JLabel vAcertos = new JLabel("0");
    private final JLabel vErros = new JLabel("0");
    private final JLabel lblNivel = new JLabel();
    private final JLabel lblProximo = new JLabel();
    private final Medidor medidor = new Medidor();
    private final BarraProgresso barraNivel = new BarraProgresso(Cores.LARANJA);
    private final Botao reset = new Botao("Resetar progresso", Cores.VERMELHO);
    private boolean confirmando;
    private final Timer cancelar;

    TelaDesempenho() {
        super("Seu progresso", "Meu Desempenho",
                "Acompanhe sua pontuação. Os números são atualizados automaticamente a cada resposta.",
                Cores.LARANJA);

        Bloco linha = new Bloco(new GridLayout(1, 3, 16, 0));
        linha.add(cartaoStat("Pontos", vPontos, Cores.AMARELO));
        linha.add(cartaoStat("Acertos", vAcertos, Cores.VERDE));
        linha.add(cartaoStat("Erros", vErros, Cores.VERMELHO));
        adicionar(linha);

        Cartao nivel = new Cartao(Cores.LARANJA);
        Bloco conteudo = new Bloco(new BorderLayout(30, 0));
        conteudo.add(medidor, BorderLayout.WEST);

        Bloco dir = new Bloco();
        dir.add(UI.espaco(8));
        dir.add(UI.rotulo("SEU NÍVEL", Cores.ETIQUETA, Cores.TEXTO_SECUNDARIO));
        lblNivel.setFont(new Font("Dialog", Font.BOLD, 32));
        lblNivel.setForeground(Cores.LARANJA);
        lblNivel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dir.add(lblNivel);
        dir.add(UI.espaco(12));
        barraNivel.setMaximumSize(new Dimension(440, 12));
        barraNivel.setPreferredSize(new Dimension(440, 12));
        dir.add(barraNivel);
        dir.add(UI.espaco(8));
        lblProximo.setFont(Cores.TEXTO_PEQUENO);
        lblProximo.setForeground(Cores.TEXTO_SECUNDARIO);
        lblProximo.setAlignmentX(Component.LEFT_ALIGNMENT);
        dir.add(lblProximo);
        dir.add(UI.espaco(16));
        dir.add(UI.texto("Níveis: Iniciante, Aprendiz (30 pts), Estudioso (80 pts) e Mestre da Lógica (150 pts).",
                440, Cores.TEXTO_SECUNDARIO));
        dir.add(UI.espaco(16));
        reset.addActionListener(e -> aoClicarReset());
        dir.add(reset);
        conteudo.add(dir, BorderLayout.CENTER);
        nivel.add(conteudo);
        adicionar(nivel);

        cancelar = new Timer(3000, e -> {
            confirmando = false;
            reset.setText("Resetar progresso");
            ((Timer) e.getSource()).stop();
        });

        Pontuacao.addOuvinte(this::atualizar);
        atualizar();
    }

    private Cartao cartaoStat(String titulo, JLabel valor, Color cor) {
        Cartao c = new Cartao(cor, 16);
        c.setBorder(new EmptyBorder(18, 26, 24, 20));
        c.add(UI.rotulo(titulo.toUpperCase(), Cores.ETIQUETA, Cores.TEXTO_SECUNDARIO));
        c.add(UI.espaco(6));
        valor.setFont(new Font("Dialog", Font.BOLD, 46));
        valor.setForeground(cor);
        valor.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(valor);
        return c;
    }

    private void aoClicarReset() {
        if (!confirmando) {
            confirmando = true;
            reset.setText("Clique de novo para confirmar");
            cancelar.restart();
        } else {
            cancelar.stop();
            confirmando = false;
            reset.setText("Resetar progresso");
            Pontuacao.resetar();
        }
    }

    private void atualizar() {
        vPontos.setText(String.valueOf(Pontuacao.getPontos()));
        vAcertos.setText(String.valueOf(Pontuacao.getAcertos()));
        vErros.setText(String.valueOf(Pontuacao.getErros()));
        medidor.setValor(Pontuacao.getPercentual(), Pontuacao.getTotal() == 0);
        lblNivel.setText(Pontuacao.getNivel());
        int prox = Pontuacao.limiteProximoNivel();
        if (prox < 0) {
            barraNivel.setValor(1);
            lblProximo.setText("Você chegou ao nível máximo. Parabéns!");
        } else {
            int base = Pontuacao.baseNivel();
            barraNivel.setValor((Pontuacao.getPontos() - base) / (double) (prox - base));
            lblProximo.setText("Faltam " + (prox - Pontuacao.getPontos()) + " pontos para o próximo nível.");
        }
    }
}
