package com.example.segurancaapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Componentes da interface
    private EditText etNome, etRA;
    private Button btnSalvarCripto, btnRecuperarCripto, btnAtualizarCripto, btnDeletarCripto;
    private TextView tvDadosCripto, tvStatusSeguranca;

    // Helpers
    private SecurityHelper securityHelper;

    // Controle de estado - dados só ficam disponíveis após recuperação
    private boolean dadosCarregados = false;
    private String currentNome = "";
    private String currentRA = "";
    private int currentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa componentes
        initViews();

        // Inicializa helpers
        securityHelper = new SecurityHelper(this);

        // CÓDIGO TEMPORÁRIO PARA GERAÇÃO DO HASH DO APK
        // ============================================
        // ATENÇÃO: método calculateApkHash() temporariamente como público no SecurityHelper
        // apenas para este teste. Mas já ajustado para private após obtenção do hash.

        /* Método adicionado apenas para possível visualização do hash no Logcat do Android studio
          try {
            String hash = securityHelper.calculateApkHash();
            android.util.Log.d("SEGURANCA_APP", "==========================================");
            android.util.Log.d("SEGURANCA_APP", "HASH DO APK PARA CONFIGURAR ANTI-TAMPERING");
            android.util.Log.d("SEGURANCA_APP", "==========================================");
            android.util.Log.d("SEGURANCA_APP", hash);
            android.util.Log.d("SEGURANCA_APP", "==========================================");*/

        /* Método adicionado anteriormente para visualização do hash na tela do smartphone na emulação
          try {
            Toast.makeText(this, "HASH DO APK:\n" + hash, Toast.LENGTH_LONG).show();
            // Copie o hash exibido e substitua no SecurityHelper.java
            // na variável "expectedHash"
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao obter hash: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }*/

        // Configura listeners
        setupListeners();

        // Atualiza status de segurança
        updateSecurityStatus();

        // Inicializar botões com estado desabilitado
        atualizarEstadoBotoes();
    }

    private void initViews() {
        etNome = findViewById(R.id.etNome);
        etRA = findViewById(R.id.etRA);

        btnSalvarCripto = findViewById(R.id.btnSalvarCripto);
        btnRecuperarCripto = findViewById(R.id.btnRecuperarCripto);
        btnAtualizarCripto = findViewById(R.id.btnAtualizarCripto);
        btnDeletarCripto = findViewById(R.id.btnDeletarCripto);

        tvDadosCripto = findViewById(R.id.tvDadosCripto);
        tvStatusSeguranca = findViewById(R.id.tvStatusSeguranca);
    }

    private void atualizarEstadoBotoes() {
        // Botões de Alterar e Deletar, somente ativos após recupração dos dados
        btnAtualizarCripto.setEnabled(dadosCarregados);
        btnDeletarCripto.setEnabled(dadosCarregados);

        // Mudar opacidade visual para indicar desabilitado
        if (!dadosCarregados) {
            btnAtualizarCripto.setAlpha(0.5f);
            btnDeletarCripto.setAlpha(0.5f);
        } else {
            btnAtualizarCripto.setAlpha(1f);
            btnDeletarCripto.setAlpha(1f);
        }
    }

    private void setupListeners() {
        // Salvar Dados Criptografados
        btnSalvarCripto.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String ra = etRA.getText().toString().trim();

            if (nome.isEmpty() || ra.isEmpty()) {
                Toast.makeText(this, "Preencha nome e RA", Toast.LENGTH_SHORT).show();
                return;
            }

            // Salvar dados criptografados
            boolean salvou = securityHelper.salvarDadosCriptografados(nome, ra);

            if (salvou) {
                // Clicar em "Recuperar e Listar Dados" para Carregá-los
                dadosCarregados = false;
                currentNome = "";
                currentRA = "";
                currentId = -1;
                atualizarEstadoBotoes();

                Toast.makeText(this, "✅ Dados salvos com criptografia AES-256!\n\nClique em 'Recuperar e Listar Dados' para visualizar.", Toast.LENGTH_LONG).show();
                limparCampos();

                // Mensagem informativa
                tvDadosCripto.setText("DADOS SALVOS COM CRIPTOGRAFIA\n\n" +
                        "✓ Os dados foram salvos de forma segura\n" +
                        "✓ Clique em 'Recuperar e Listar Dados' para visualizar\n" +
                        "✓ Após recuperar, você poderá alterar ou deletar\n\n" +
                        "Protegido com AES-256");
                tvDadosCripto.setVisibility(View.VISIBLE);
                updateSecurityStatus();
            } else {
                Toast.makeText(this, "Erro ao salvar dados criptografados", Toast.LENGTH_SHORT).show();
            }
        });

        // Recuperar e Listar para Carregar dados na memória
        btnRecuperarCripto.setOnClickListener(v -> {
            String[] dados = securityHelper.recuperarDadosCriptografados();

            if (dados[0].isEmpty() || dados[1].isEmpty()) {
                tvDadosCripto.setText("NENHUM DADO CRIPTOGRAFADO ENCONTRADO!\n\n" +
                        "Por favor, salve alguns dados primeiro clicando em\n" +
                        "'Salvar Dados Criptografados'.");
                tvDadosCripto.setVisibility(View.VISIBLE);
                dadosCarregados = false;
                currentNome = "";
                currentRA = "";
                atualizarEstadoBotoes();
                Toast.makeText(this, "Nenhum dado encontrado! Salve dados primeiro.", Toast.LENGTH_LONG).show();
            } else {
                // CARREGAR DADOS NA MEMÓRIA - Agora ficam disponíveis para alterar/deletar
                currentNome = dados[0];
                currentRA = dados[1];
                currentId = 1; // ID fictício para controle
                dadosCarregados = true;
                atualizarEstadoBotoes();

                exibirDadosRecuperados();
                Toast.makeText(this, "✅ Dados recuperados e carregados na memória!\n\nAgora você pode ALTERAR ou DELETAR estes dados.", Toast.LENGTH_LONG).show();
                updateSecurityStatus();
            }
        });

        // Alterar Dados Criptografados - Só funciona se dados foram carregados
        btnAtualizarCripto.setOnClickListener(v -> {
            // Verificar se dados foram carregados primeiro
            if (!dadosCarregados) {
                Toast.makeText(this, "Nenhum dado carregado!\n\nClique em 'Recuperar e Listar Dados' primeiro.", Toast.LENGTH_LONG).show();
                return;
            }

            String nome = etNome.getText().toString().trim();
            String ra = etRA.getText().toString().trim();

            if (nome.isEmpty() || ra.isEmpty()) {
                Toast.makeText(this, "Preencha os novos dados para atualizar", Toast.LENGTH_SHORT).show();
                return;
            }

            // Atualizar dados criptografados no storage
            boolean atualizou = securityHelper.atualizarDadosCriptografados(nome, ra);

            if (atualizou) {
                // Atualizar dados na memória
                currentNome = nome;
                currentRA = ra;

                exibirDadosRecuperados();
                Toast.makeText(this, "Dados atualizados com sucesso!\n\nOs dados continuam carregados na memória.", Toast.LENGTH_LONG).show();
                limparCampos();
                updateSecurityStatus();
            } else {
                Toast.makeText(this, "Erro ao atualizar dados", Toast.LENGTH_SHORT).show();
            }
        });

        // Deletar Dados Criptografados - Só funciona se dados foram carregados
        btnDeletarCripto.setOnClickListener(v -> {
            // Verificar se dados foram carregados primeiro
            if (!dadosCarregados) {
                Toast.makeText(this, "Nenhum dado carregado!\n\nClique em 'Recuperar e Listar Dados' primeiro.", Toast.LENGTH_LONG).show();
                return;
            }

            // Confirmar deleção
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirmar Deleção")
                    .setMessage("Tem certeza que deseja deletar os dados criptografados?\n\n" +
                            "Dados atuais carregados:\n" +
                            "Nome: " + currentNome + "\n" +
                            "RA: " + currentRA + "\n\n" +
                            "Esta ação não pode ser desfeita!")
                    .setPositiveButton("Sim, deletar", (dialog, which) -> {
                        boolean deletou = securityHelper.deletarDadosCriptografados();

                        if (deletou) {
                            // Limpar dados da memória
                            dadosCarregados = false;
                            currentNome = "";
                            currentRA = "";
                            currentId = -1;
                            atualizarEstadoBotoes();

                            tvDadosCripto.setText("DADOS DELETADOS COM SUCESSO!\n\n" +
                                    "✓ Os dados criptografados foram removidos\n" +
                                    "✓ Clique em 'Recuperar' para verificar\n" +
                                    "✓ Para novos dados, clique em 'Salvar'\n\n" +
                                    "Sistema seguro - Nenhum dado armazenado");
                            tvDadosCripto.setVisibility(View.VISIBLE);
                            Toast.makeText(MainActivity.this, "Dados deletados com sucesso!", Toast.LENGTH_LONG).show();
                            limparCampos();
                            updateSecurityStatus();
                        } else {
                            Toast.makeText(MainActivity.this, "Erro ao deletar dados", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void exibirDadosRecuperados() {
        String dados = "DADOS RECUPERADOS E CARREGADOS NA MEMÓRIA\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Status: Dados disponíveis para operações\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Nome: " + currentNome + "\n" +
                "RA: " + currentRA + "\n" +
                "ID Temporário: " + currentId + "\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "OPERAÇÕES DISPONÍVEIS AGORA:\n" +
                "ALTERAR DADOS CRIPTOGRAFADOS\n" +
                "DELETAR DADOS CRIPTOGRAFADOS\n\n" +
                "Protegido com AES-256\n" +
                "Integridade verificada\n" +
                "Modo seguro ativo";
        tvDadosCripto.setText(dados);
        tvDadosCripto.setVisibility(View.VISIBLE);
    }

    private void updateSecurityStatus() {
        String status = "STATUS DE SEGURANÇA\n\n";
        status += "Criptografia: ATIVA (AES-256)\n";
        status += "Modo de operação: GCM\n";
        status += "Tamanho da chave: 256 bits\n";
        status += "Anti-tampering: " + securityHelper.getStatusIntegridade() + "\n";
        status += "Detecção Root: " + securityHelper.getRootStatus() + "\n";
        status += "Ofuscação: Ativa em modo Release\n\n";

        // Verificar se há dados armazenados
        String[] dados = securityHelper.recuperarDadosCriptografados();
        if (!dados[0].isEmpty() && !dados[1].isEmpty()) {
            status += "Dados armazenados: SIM\n";
            status += "Proteção: ATIVA\n";

            if (dadosCarregados) {
                status += "Dados na memória: CARREGADOS\n";
                status += "Operações permitidas: ALTERAR e DELETAR\n";
            } else {
                status += "Dados na memória: NÃO CARREGADOS\n";
                status += "Clique em 'Recuperar' para habilitar operações\n";
            }
        } else {
            status += "Dados armazenados: NÃO\n";
            status += " Nenhum dado criptografado encontrado\n";
            status += "Clique em 'Salvar' para criar dados\n";
        }

        status += "\n App executando em modo seguro";
        tvStatusSeguranca.setText(status);
    }

    private void limparCampos() {
        etNome.setText("");
        etRA.setText("");
        etNome.requestFocus();
    }
}