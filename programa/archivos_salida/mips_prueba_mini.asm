.data
    .align 2
    newline: .asciiz "\n"
    resultado_global: .word 0
    mi_lista: .word 0, 0, 0, 0

.text
    .align 2
    .globl main

main:
    # Asignar a array global mi_lista[0][0] = 10
    li $t0, 10
    sw $t0, mi_lista + 0
    # Asignar a array global mi_lista[0][1] = 20
    li $t0, 20
    sw $t0, mi_lista + 4
    # Asignar a array global mi_lista[1][0] = 30
    li $t0, 30
    sw $t0, mi_lista + 8
    # Asignar a array global mi_lista[1][1] = 40
    li $t0, 40
    sw $t0, mi_lista + 12

    jal NAVIDAD
    li $v0, 10         # Syscall exit
    syscall

factorial:
    # Prólogo de factorial
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro n

    lw $t0, 0($sp)
    li $t1, 1
    sle $t2, $t0, $t1
    sw $t2, 4($sp)
    lw $t0, 4($sp)
    bnez $t0, L1    # Saltar si t0 != 0
    j L2
L1:
    li $v0, 1
    # Secuencia de retorno
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
    j L0
L2:
    lw $t0, 0($sp)
    li $t1, 1
    sub $t2, $t0, $t1
    sw $t2, 8($sp)
    lw $a0, 8($sp)
    jal factorial    # Llamar factorial
    sw $v0, 12($sp)
    lw $t0, 0($sp)
    lw $t1, 12($sp)
    mul $t2, $t0, $t1
    sw $t2, 16($sp)
    lw $v0, 16($sp)
    # Secuencia de retorno
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
L0:
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

suma_rango:
    # Prólogo de suma_rango
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256
    sw $a0, 0($sp)    # Guardar parámetro inicio
    sw $a1, 4($sp)    # Guardar parámetro fin

    li $t0, 0
    sw $t0, 8($sp)
    lw $t0, 0($sp)
    sw $t0, 12($sp)
L3:
    lw $t0, 12($sp)
    lw $t1, 4($sp)
    sle $t2, $t0, $t1
    sw $t2, 16($sp)
    lw $t0, 16($sp)
    bnez $t0, L4    # Saltar si t4 != 0
    j L5
L4:
    lw $t0, 8($sp)
    lw $t1, 12($sp)
    add $t2, $t0, $t1
    sw $t2, 20($sp)
    lw $t0, 20($sp)
    sw $t0, 8($sp)
    lw $t0, 12($sp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 24($sp)
    lw $t0, 24($sp)
    sw $t0, 12($sp)
    j L3
L5:
    lw $v0, 8($sp)
    # Secuencia de retorno
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

NAVIDAD:
    # Prólogo de NAVIDAD
    addi $sp, $sp, -256
    sw $ra, 252($sp)
    sw $fp, 248($sp)
    addi $fp, $sp, 256

    li $a0, 5
    jal factorial    # Llamar factorial
    sw $v0, 0($sp)
    lw $t0, 0($sp)
    sw $t0, 4($sp)
    lw $a0, 4($sp)
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    li $a0, 1
    li $a1, 10
    jal suma_rango    # Llamar suma_rango
    sw $v0, 8($sp)
    lw $t0, 8($sp)
    sw $t0, 12($sp)
    lw $a0, 12($sp)
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    lw $t0, 4($sp)
    lw $t1, 12($sp)
    add $t2, $t0, $t1
    sw $t2, 16($sp)
    lw $t0, 16($sp)
    sw $t0, resultado_global
    lw $a0, resultado_global
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    li $t0, 1
    li $t1, 0
    # Acceso a array global mi_lista[1][0]
    li $t2, 2
    mul $t3, $t0, $t2
    add $t3, $t3, $t1
    sll $t3, $t3, 2
    la $t4, mi_lista
    add $t4, $t4, $t3
    lw $t5, 0($t4)
    sw $t5, 20($sp)
    lw $t0, 20($sp)
    sw $t0, 24($sp)
    lw $a0, 24($sp)
    li $v0, 1          # Syscall print_int
    syscall
    la $a0, newline
    li $v0, 4
    syscall
    # Epílogo por defecto
    lw $ra, 252($sp)
    lw $fp, 248($sp)
    addi $sp, $sp, 256
    jr $ra

