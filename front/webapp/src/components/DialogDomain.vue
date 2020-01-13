<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-dialog v-model="show" lazy persistent width="45%">
        <v-card class="record-card pa-3">
            <v-card-title class="font-weight-bold pa-0 pb-4">
                <v-flex align-self-center md6 style="font-size: larger">
                    {{ action === 'create' ? 'Создать запись' : 'Редактировать запись'}}
                </v-flex>
                <v-spacer></v-spacer>
                <v-icon @click="closeCard">close</v-icon>
            </v-card-title>
            <v-card-text>
                <v-layout row class="pa-0 mb-2" style="align-items: center">
                    <v-flex align-self-center md2>
                        ID
                    </v-flex>
                    <v-flex md6 class="value-font">
                        <span>{{visibleUnitId}}</span>
                    </v-flex>
                </v-layout>
                <v-layout row class="pa-0 mb-2" style="align-items: center">
                    <v-flex md2>
                        Домен
                    </v-flex>
                    <v-flex md6 class="value-font">
                        <v-textarea
                                v-model="unitInternal.domain"
                                auto-grow
                                rows="1"
                        ></v-textarea>
                    </v-flex>
                </v-layout>
            </v-card-text>
            <v-card-actions>
                <v-layout row>
                    <v-spacer></v-spacer>
                    <v-btn color="" flat @click="closeCard">Отмена</v-btn>
                    <v-btn color="primary" @click="save" :disabled="!isValid">Сохранить</v-btn>
                </v-layout>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script>
    export default {
        props: ['value', "action", "unit"],

        data() {
            return {
                unitInternal: this.newUnit(),
                newIdPrefix: "new"
            }
        },

        filters: {
            noData(v) {
                return (v == null || v == '') ? '-' : v;
            }
        },

        computed: {
            show: {
                set: function (v) {
                    this.$emit('input', v)
                },
                get: function () {
                    return this.value;
                }
            },

            visibleUnitId(){
                let id = this.unitInternal.id == null ? "-" : this.unitInternal.id;
                if (id.startsWith(this.newIdPrefix))
                    id = "-";
                return id;
            },

            isValid() {
                return !!this.unitInternal.domain;
            }
        },

        watch: {
            unit(v) {
                this.unitInternal = this.newUnit(v);
            }
        },

        methods: {
            closeCard() {
                this.show = false;
            },

            newUnit(unit){
                return Object.assign({id: null, domain: null}, unit);
            },

            save() {
                let newUnit = this.newUnit(this.unitInternal);
                this.$emit('updateUnit', newUnit);
                this.closeCard();
            }
        }
    }
</script>

<style>
    .value-font .v-input .v-textarea {
        display: none;
    }
    .value-font .v-input.v-textarea {
        padding-top: 0;
    }

    .value-font .v-input.v-input--checkbox {
        margin: 0;
        padding: 0;
    }
    .value-font .v-input.v-input--checkbox .v-messages{
        display: none;
    }

    .value-font .v-text-field__details {
        display: none;
    }
</style>

<style scoped>
    .name-font {
        color: rgba(0,0,0,.54)
    }
</style>