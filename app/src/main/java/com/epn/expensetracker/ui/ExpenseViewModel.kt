package com.epn.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.epn.expensetracker.data.local.ExpenseEntity
import com.epn.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    recordatorioActivoInicial: Boolean = true,
    horaRecordatorioInicial: Int = 21,
    minutoRecordatorioInicial: Int = 0
) : ViewModel() {

    private val _monto = MutableStateFlow("")
    val monto: StateFlow<String> = _monto.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow("Comida")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    // Modo edición
    private val _gastoEditando = MutableStateFlow<ExpenseEntity?>(null)
    val gastoEditando: StateFlow<ExpenseEntity?> = _gastoEditando.asStateFlow()

    private val _recordatorioActivo = MutableStateFlow(recordatorioActivoInicial)
    val recordatorioActivo: StateFlow<Boolean> = _recordatorioActivo.asStateFlow()

    private val _horaRecordatorio = MutableStateFlow(horaRecordatorioInicial)
    val horaRecordatorio: StateFlow<Int> = _horaRecordatorio.asStateFlow()

    private val _minutoRecordatorio = MutableStateFlow(minutoRecordatorioInicial)
    val minutoRecordatorio: StateFlow<Int> = _minutoRecordatorio.asStateFlow()

    val gastos = repository.todosLosGastos
    val total = repository.totalGeneral

    val categorias = listOf("Comida", "Transporte", "Entretenimiento", "Servicios", "Otros")

    fun actualizarMonto(valor: String) {
        if (valor.isEmpty() || valor.matches(Regex("^\\d*\\.?\\d*$"))) {
            _monto.value = valor
        }
    }

    fun actualizarDescripcion(valor: String) {
        _descripcion.value = valor
    }

    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }

    fun iniciarEdicion(gasto: ExpenseEntity) {
        _gastoEditando.value = gasto
        _monto.value = gasto.monto.toString()
        _descripcion.value = gasto.descripcion
        _categoriaSeleccionada.value = gasto.categoria
    }

    fun cancelarEdicion() {
        _gastoEditando.value = null
        _monto.value = ""
        _descripcion.value = ""
        _categoriaSeleccionada.value = "Comida"
    }

    fun cambiarEstadoRecordatorio(activo: Boolean) {
        _recordatorioActivo.value = activo
    }

    fun actualizarHoraRecordatorio(hora: Int, minuto: Int) {
        _horaRecordatorio.value = hora
        _minutoRecordatorio.value = minuto
    }

    fun guardarGasto() {
        val montoDouble = _monto.value.toDoubleOrNull()
        if (montoDouble == null || montoDouble <= 0) return
        if (_descripcion.value.isBlank()) return

        viewModelScope.launch {
            val editando = _gastoEditando.value

            if (editando == null) {
                repository.agregar(
                    ExpenseEntity(
                        monto = montoDouble,
                        descripcion = _descripcion.value.trim(),
                        categoria = _categoriaSeleccionada.value
                    )
                )
            } else {
                repository.actualizar(
                    editando.copy(
                        monto = montoDouble,
                        descripcion = _descripcion.value.trim(),
                        categoria = _categoriaSeleccionada.value
                    )
                )
            }

            cancelarEdicion()
        }
    }

    fun eliminarGasto(gasto: ExpenseEntity) {
        viewModelScope.launch { repository.eliminar(gasto) }
    }
}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val recordatorioActivo: Boolean,
    private val horaRecordatorio: Int,
    private val minutoRecordatorio: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(
                repository, recordatorioActivo, horaRecordatorio, minutoRecordatorio
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
