package ru.andrewb.charm.back.mapper;

public interface Mapper<From, To> {

    To map(From from);

    To map(From from, To to);
}
